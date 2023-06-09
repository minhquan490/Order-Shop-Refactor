import { IncomingMessage, ServerResponse } from "http";

export abstract class HttpMessageHandler {
    abstract handle(request: IncomingMessage, response: ServerResponse<IncomingMessage>): Promise<ServerResponse<IncomingMessage> | string>
}