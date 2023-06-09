package com.bachlinh.order.web.service.impl;

import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DependenciesInitialize;
import com.bachlinh.order.annotation.ServiceComponent;
import com.bachlinh.order.entity.EntityFactory;
import com.bachlinh.order.entity.model.Product;
import com.bachlinh.order.entity.model.ProductMedia;
import com.bachlinh.order.entity.model.Product_;
import com.bachlinh.order.exception.http.BadVariableException;
import com.bachlinh.order.exception.http.ResourceNotFoundException;
import com.bachlinh.order.repository.ProductMediaRepository;
import com.bachlinh.order.repository.ProductRepository;
import com.bachlinh.order.service.AbstractService;
import com.bachlinh.order.service.container.ContainerWrapper;
import com.bachlinh.order.service.container.DependenciesResolver;
import com.bachlinh.order.web.dto.form.common.FileFlushForm;
import com.bachlinh.order.web.dto.form.common.FileUploadForm;
import com.bachlinh.order.web.dto.resp.ProductMediaResp;
import com.bachlinh.order.web.dto.resp.ResourceResp;
import com.bachlinh.order.web.service.business.FileUploadService;
import com.bachlinh.order.web.service.business.ImageCompressService;
import com.bachlinh.order.web.service.common.ProductMediaService;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ServiceComponent
@ActiveReflection
public class ProductMediaServiceImpl extends AbstractService<ResourceResp, FileUploadForm> implements ProductMediaService, FileUploadService, ImageCompressService {
    private static final String TEMP_FILE_EXTENSION = ".bin";

    private String tempFilePath;
    private String resourceFilePath;
    private Integer maxFileSize;
    private Integer serveFileSize;
    private ProductRepository productRepository;
    private ProductMediaRepository productMediaRepository;
    private EntityFactory entityFactory;
    private ThreadPoolTaskExecutor taskExecutor;

    @ActiveReflection
    @DependenciesInitialize
    public ProductMediaServiceImpl(ThreadPoolTaskExecutor executor, ContainerWrapper wrapper, @Value("${active.profile}") String profile) {
        super(executor, wrapper, profile);
    }

    @Override
    protected ResourceResp doSave(FileUploadForm param) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ResourceResp doUpdate(FileUploadForm param) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ResourceResp doDelete(FileUploadForm param) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected ResourceResp doGetOne(FileUploadForm param) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <K, X extends Iterable<K>> X doGetList(FileUploadForm param) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void inject() {
        DependenciesResolver resolver = getContainerResolver().getDependenciesResolver();
        if (tempFilePath == null) {
            tempFilePath = getEnvironment().getProperty("server.resource.folder.temp");
        }
        if (resourceFilePath == null) {
            resourceFilePath = getEnvironment().getProperty("server.resource.folder.data");
        }
        if (productRepository == null) {
            productRepository = resolver.resolveDependencies(ProductRepository.class);
        }
        if (productMediaRepository == null) {
            productMediaRepository = resolver.resolveDependencies(ProductMediaRepository.class);
        }
        if (entityFactory == null) {
            entityFactory = resolver.resolveDependencies(EntityFactory.class);
        }
        if (taskExecutor == null) {
            taskExecutor = resolver.resolveDependencies(ThreadPoolTaskExecutor.class);
        }
        if (maxFileSize == null) {
            maxFileSize = Integer.valueOf(getEnvironment().getProperty("server.resource.file.upload.maxsize"));
        }
        if (serveFileSize == null) {
            serveFileSize = Integer.valueOf(getEnvironment().getProperty("server.resource.file.serve.size"));
        }
    }

    /**
     * File name pattern {productId}-{fileId}-{part}
     */
    @Override
    public void handleMultipartFile(FileUploadForm file) throws IOException {
        byte[] data = Base64.getDecoder().decode(file.base64Data());
        if (data.length > maxFileSize) {
            throw new BadVariableException("Max request file size");
        }
        String[] infoPart = file.fileName().split("-");

        createProductTempFolder(infoPart);

        createFileTempFolder(infoPart);

        Path path = Files.createFile(Path.of(tempFilePath, infoPart[0], infoPart[1], infoPart[2].concat(TEMP_FILE_EXTENSION)));
        FileCopyUtils.copy(data, path.toFile());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public void catAndFlushFile(FileFlushForm form) throws IOException {
        Map<String, Object> condition = new HashMap<>(1);
        condition.put(Product_.ID, form.productId());
        Product product = productRepository.getProductByCondition(condition);

        if (product == null) {
            throw new ResourceNotFoundException("Product with id [" + form.productId() + "] not found", "");
        }

        boolean productTempFolderExist = Files.exists(Path.of(tempFilePath, form.productId()));
        if (!productTempFolderExist) {
            throw new BadVariableException("Product has id [" + form.productId() + "] not existed");
        }
        boolean fileTempFolderExist = Files.exists(Path.of(tempFilePath, form.productId(), form.fileId()));
        if (!fileTempFolderExist) {
            throw new BadVariableException("You not upload any thing to server");
        }
        Path path = Path.of(tempFilePath, form.productId(), form.fileId());
        File[] fs = path.toFile().listFiles();
        if (fs == null || fs.length == 0) {
            return;
        }
        SortMachine sortMachine = new SortMachine();
        sortMachine.sort(fs, 0, fs.length - 1);

        ProductMedia productMedia = entityFactory.getEntity(ProductMedia.class);
        productMedia.setContentType(form.contentType());

        path = Path.of(resourceFilePath, form.productId(), UUID.randomUUID().toString().concat(resolveExtension(form.contentType())));
        Files.createFile(path);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(path.toFile(), "rw");
             FileChannel channel = randomAccessFile.getChannel()) {
            for (File f : fs) {
                try (FileOutputStream stream = new FileOutputStream(f)) {
                    FileChannel fileChannel = stream.getChannel();
                    fileChannel.transferTo(0, fileChannel.size(), channel);
                }
                f.deleteOnExit();
            }
            productMedia.setContentLength(channel.size());
        }
        productMedia.setProduct(product);
        productMedia.setUrl(path.toString());
        productMediaRepository.saveMedia(productMedia);
    }

    @Override
    public ProductMediaResp serveResource(String id, long position) {
        ProductMediaResp resp = new ProductMediaResp();
        ByteBuffer buffer;
        int mediaId;
        try {
            mediaId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new BadVariableException("Id of media must be int");
        }
        ProductMedia media = productMediaRepository.loadMedia(mediaId);
        if (media.getUrl().endsWith(".mp4")) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(Path.of(media.getUrl()).toFile(), "r");
                 FileChannel channel = randomAccessFile.getChannel()) {
                if (channel.size() < serveFileSize) {
                    buffer = ByteBuffer.allocate((int) channel.size());
                } else {
                    buffer = ByteBuffer.allocate(serveFileSize);
                }
                channel.position(position);
                channel.read(buffer);
                resp.setTotalSize(channel.size());
            } catch (IOException e) {
                throw new ResourceNotFoundException("Resource has id [" + id + "] did not existed", "");
            }
        } else {
            try {
                buffer = ByteBuffer.wrap(compressImage(media.getUrl(), media.getContentType()));
            } catch (IOException e) {
                throw new ResourceNotFoundException("Resource has id [" + id + "] did not existed", "");
            }
        }
        resp.setData(buffer.array());
        resp.setContentType(media.getContentType());
        resp.setComplete(true);
        buffer.clear();
        return resp;
    }

    @Override
    public byte[] compressImage(String imagePath, String contentType) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(imagePath));
        Image resultImage = bufferedImage.getScaledInstance(300, 300, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultImage, 0, 0, null);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(outputImage, contentType.split("/")[1], arrayOutputStream);
        return arrayOutputStream.toByteArray();
    }

    private void createProductTempFolder(String[] paths) throws IOException {
        Path path = Path.of(tempFilePath, paths[0]);
        boolean productTempFolderExist = Files.exists(path);
        if (!productTempFolderExist) {
            Files.createDirectory(path);
        }
    }

    private void createFileTempFolder(String[] paths) throws IOException {
        Path path = Path.of(tempFilePath, paths[0], paths[1]);
        boolean folderFileTempExit = Files.exists(path);
        if (folderFileTempExit) {
            Files.delete(path);
        } else {
            Files.createDirectory(path);
        }
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_GIF_VALUE -> ".gif";
            case "audio/mp4", "video/mp4", "application/mp4" -> ".mp4";
            default -> throw new BadVariableException("Content type [" + contentType + "] is not supported");
        };
    }

    private static class SortMachine {

        SortMachine() {
        }

        private void merge(File[] arr, int left, int mid, int right) {

            int temp1 = mid - left + 1;
            int temp2 = right - mid;

            File[] tempFilesLeft = new File[temp1];
            File[] tempFilesRight = new File[temp2];

            System.arraycopy(arr, left, tempFilesLeft, 0, temp1);
            System.arraycopy(arr, mid + left, tempFilesRight, 0, temp2);

            int i = 0;
            int j = 0;
            int k = 1;
            while (i < temp1 && j < temp2) {
                String leftName = tempFilesLeft[i].getName();
                String rightName = tempFilesRight[j].getName();
                if (leftName.compareTo(rightName) <= 0) {
                    arr[k] = tempFilesLeft[i];
                    i++;
                } else {
                    arr[k] = tempFilesRight[j];
                    j++;
                }
                k++;
            }
            while (i < temp1) {
                arr[k] = tempFilesLeft[i];
                i++;
                k++;
            }
            while (j < temp2) {
                arr[k] = tempFilesRight[j];
                j++;
                k++;
            }
        }

        void sort(File[] arr, int left, int right) {
            if (left < right) {
                int mid = left + (right - left) / 2;
                sort(arr, left, mid);
                sort(arr, mid + left, right);
                merge(arr, left, mid, right);
            }
        }
    }
}
