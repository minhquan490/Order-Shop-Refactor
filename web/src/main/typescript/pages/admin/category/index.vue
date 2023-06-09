<script lang="ts">
import { Subscription, map, of } from 'rxjs';
import { Category } from '~/types/category.type';
import { ErrorResponse } from '~/types/error-response.type';
import { TableHeaders } from '~/types/table-header.type';
import { TableActionCallback } from '~/types/table-store.type';

export default {
  setup() {
    const pathPrefix = '/admin/category';
    const categoryStore = useCategoryStore();
    const tableAction: TableActionCallback = new TableActionCallback(categoryStore, "selectedCategory");
    const clientProvider = useHttpClient().value;
    const serverUrl = useAppConfig().serverUrl;
    const deleteUrl = `${serverUrl}${pathPrefix}/delete`;
    const updateUrl = `${serverUrl}${pathPrefix}/update`;
    const createUrl = `${serverUrl}${pathPrefix}/create`;
    const listUrl = `${serverUrl}${pathPrefix}/list`;
    return {
      categoryStore,
      tableAction,
      clientProvider,
      deleteUrl,
      updateUrl,
      createUrl,
      listUrl
    }
  },
  data() {
    const pageData: PageData = new PageData();
    const subscriptions: Array<Subscription> = [];
    return {
      pageData,
      subscriptions
    }
  },
  beforeMount() {
    const subscription = of(this.listUrl)
      .pipe(
        map(url => this.clientProvider(url)),
        map(service => service.get<undefined, Category[]>()),
        map(resp => {
          if (resp.isError || resp.getResponse === null) {
            return [];
          }
          return resp.getResponse;
        }),
        map(result => {
          const pageData = new PageData();
          pageData.tableHeaders = [];
          pageData.tableData = result as Category[];
          return pageData;
        }),
        map(data => {
          const tableHeaders: TableHeaders[] = [
            {
              isId: true,
              isImg: false,
              name: 'ID',
              dataPropertyName: 'id'
            },
            {
              isId: false,
              isImg: false,
              name: 'Name',
              dataPropertyName: 'name'
            }
          ];
          data.tableHeaders = tableHeaders;
          return data;
        })
      )
      .subscribe(result => {
        if (this.pageData) {
          this.pageData.tableData = result.tableData;
          this.pageData.tableHeaders = result.tableHeaders;
        }
      })
    this.subscriptions?.push(subscription);
  },
  beforeUnmount() {
    if (this.subscriptions) {
      this.subscriptions.forEach(sub => sub.unsubscribe());
    }
    if (this.pageData) {
      this.pageData.tableData = [];
      this.pageData.tableHeaders = [];
    }
  },
  methods: {
    deleteCategory(event: MouseEvent) {
      event.preventDefault();
      if (!this.categoryStore.selectedCategory) {
        return;
      }
      of(this.deleteUrl)
        .pipe(
          map(url => this.clientProvider(url)),
          map(client => {
            return client.delete<DeleteRequest, ErrorResponse>({
              id: this.categoryStore.getCategory?.id as string
            });
          })
        )
        .subscribe(resp => {
          if (resp.isError && this.pageData) {
            this.pageData.deleteErrorMessages = resp.getResponse === null ? [] : resp.getResponse.messages;
            setTimeout(() => {
              if (this.pageData) {
                this.pageData.deleteErrorMessages = [];
              }
            }, 5000);
          }
        })
    },
    updateCategory(event: MouseEvent) {
      event.preventDefault();
      if (!this.categoryStore.selectedCategory) {
        return;
      }
      of(this.updateUrl)
        .pipe(
          map(url => this.clientProvider(url)),
          map(client => {
            const updatedValue = this.categoryStore.getCategory as Category;
            return client.put<UpdateRequest, CategoryResponse | ErrorResponse>({
              id: updatedValue.id,
              name: updatedValue.name
            });
          })
        )
        .subscribe(value => {
          if (value.isError && this.pageData) {
            this.pageData.updateErrorMessage = (value.getResponse as ErrorResponse).messages;
            setTimeout(() => {
              if (this.pageData) {
                this.pageData.updateErrorMessage = [];
              }
            }, 5000);
          }
          if (value instanceof CategoryResponse && this.pageData) {
            this.pageData.tableData.forEach(data => {
              if (data.id === value.id) {
                data.name = value.name;
              }
            });
          }
        });
    },
    createCategory(event: MouseEvent) {
      event.preventDefault();
      const ref = this.$refs['create-category'] as HTMLInputElement;
      if (ref.value.length === 0) {
        return;
      }
      of(this.createUrl)
        .pipe(
          map(url => this.clientProvider(url)),
          map(client => client.post<CreateRequest, CategoryResponse | ErrorResponse>({
            name: ref.value
          }))
        )
        .subscribe(resp => {
          if (resp.isError && this.pageData) {
            this.pageData.createErrorMessage = (resp.getResponse as ErrorResponse).messages;
            setTimeout(() => {
              if (this.pageData) {
                this.pageData.createErrorMessage = [];
              }
            }, 5000);
          }
          if (resp instanceof CategoryResponse && this.pageData) {
            this.pageData.tableData.push(resp);
          }
        })
    },
    resetState(event: MouseEvent) {
      event.preventDefault();
      this.categoryStore.setCategory(undefined);
    }
  },
}

class PageData {
  tableHeaders: Array<TableHeaders> = [];
  tableData: Array<Category> = [];
  deleteErrorMessages: Array<string> = [];
  updateErrorMessage: Array<string> = [];
  createErrorMessage: Array<string> = [];
}

type DeleteRequest = {
  id: string
}

type UpdateRequest = {
  id: string,
  name: string
}

type CreateRequest = {
  name: string
}

class CategoryResponse {
  id!: string;
  name!: string;
}
</script>

<template>
  <div class="category">
    <div class="pt-20 px-8 grid grid-cols-4 gap-3 h-full">
      <div class="col-span-4 -m-1 px-2">
        <Breadcrumb />
      </div>
      <div class="col-span-3">
        <DataTable :height="'50vh'" :setter="tableAction" :headers="pageData?.tableHeaders" :datas="pageData?.tableData"
          tableTittle="Categories" tableIconName="quill:label" />
      </div>
      <div class="col-span-1 grid grid-rows-2 gap-3">
        <div class="rounded-md p-6 row-span-1 update">
          <div class="flex items-center justify-center pb-3">
            <span class="hover:cursor-default">Category information</span>
          </div>
          <div class="pb-4">
            <span v-if="!categoryStore.selectedCategory" class="hover:cursor-default">ID:</span>
            <span v-if="categoryStore.selectedCategory" v-text="`ID: ${categoryStore.selectedCategory.id}`"
              class="hover:cursor-default"></span>
          </div>
          <div class="flex items-center pb-4">
            <span class="hover:cursor-default pr-3">Name:</span>
            <input class="leading-10 w-full px-4 bg-gray-200 outline-none rounded-md" type="text"
              :value="categoryStore.selectedCategory?.name">
          </div>
          <div class="grid grid-cols-3 gap-2 w-full pt-6">
            <div class="col-span-1">
              <ButtonBasic :clickFunc="resetState" :name="'Reset'" :textSize="'0.875rem'" :color="'white'" :bgColor="'rgb(22 163 74)'" />
            </div>
            <div class="col-span-1">
              <ButtonBasic :clickFunc="deleteCategory" :name="'Delete'" :textSize="'0.875rem'" :color="'white'" :bgColor="'rgb(220 38 38)'" />
            </div>
            <div class="col-span-1">
              <ButtonBasic :clickFunc="updateCategory" :name="'Update'" :textSize="'0.875rem'" :color="'white'" :bgColor="'rgb(37 99 235)'" />
            </div>
          </div>
        </div>
        <div class="rounded-md p-4 row-span-1 grid grid-rows-3 update">
          <div class="flex items-start justify-center row-span-1">
            <span class="hover:cursor-default">Create new category</span>
          </div>
          <div class="flex flex-col row-span-1">
            <span class="hover:cursor-default pb-3">
              Name of category
            </span>
            <input ref="create-category" type="text" class="w-full leading-10 rounded-md bg-slate-200 outline-none px-4">
          </div>
          <div class="row-span-1 flex items-center justify-center">
            <div class="w-full">
              <ButtonBasic :clickFunc="createCategory" :name="'Create'" :textSize="'0.875rem'" :color="'white'" :bgColor="'rgb(96 165 250)'" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
$page_bg: #f4f6f9;

.category {
  background-color: $page_bg;
  padding-bottom: 1.5rem;

  & .update {
    box-shadow: rgba(0, 0, 0, 0.16) 0px 3px 6px, rgba(0, 0, 0, 0.23) 0px 3px 6px;
  }
}
</style>
