package com.hubbox.demo.controller;

import static com.hubbox.demo.util.Constants.CONTEXT_PATH;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.hubbox.demo.dto.request.DeviceCategoryCreateRequest;
import com.hubbox.demo.dto.request.DeviceCategoryUpdateRequest;
import com.hubbox.demo.dto.response.DeviceCategoryResponse;
import com.hubbox.demo.service.DeviceCategoryService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.HttpMethod;
import io.javalin.openapi.OpenApi;
import io.javalin.openapi.OpenApiContent;
import io.javalin.openapi.OpenApiParam;
import io.javalin.openapi.OpenApiRequestBody;
import io.javalin.openapi.OpenApiResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class DeviceCategoryController extends AbstractController {
    private final DeviceCategoryService categoryService;

    @Inject
    public DeviceCategoryController(DeviceCategoryService categoryService) {
        super("categories");
        this.categoryService = categoryService;
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.post(buildPath(), this::createCategory);
        app.get(buildPath(), this::getAllCategories);
        app.get(buildPath("{id}"), this::getCategory);
        app.put(buildPath("{id}"), this::updateCategory);
        app.delete(buildPath("{id}"), this::deleteCategory);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/categories",
        methods = {HttpMethod.POST},
        summary = "Create a new device category",
        operationId = "createCategory",
        tags = {"Device Categories"},
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCategoryCreateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", content = {@OpenApiContent(from = DeviceCategoryResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request")
        }
    )
    private void createCategory(Context ctx) {
        DeviceCategoryCreateRequest request = ctx.bodyAsClass(DeviceCategoryCreateRequest.class);
        DeviceCategoryResponse response = categoryService.createCategory(request);
        ctx.status(201).json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/categories",
        methods = {HttpMethod.GET},
        summary = "Get all categories",
        operationId = "getAllCategories",
        tags = {"Device Categories"},
        responses = {
            @OpenApiResponse(
                status = "200",
                content = {@OpenApiContent(from = DeviceCategoryResponse[].class)}
            )
        }
    )
    private void getAllCategories(Context ctx) {
        List<DeviceCategoryResponse> responses = categoryService.getAllCategories();
        ctx.json(responses);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/categories/{id}",
        methods = {HttpMethod.GET},
        summary = "Get category by ID",
        operationId = "getCategory",
        tags = {"Device Categories"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Category ID")
        },
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceCategoryResponse.class)}),
            @OpenApiResponse(status = "404", description = "Category not found")
        }
    )
    private void getCategory(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceCategoryResponse response = categoryService.getCategory(id);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/categories/{id}",
        methods = {HttpMethod.PUT},
        summary = "Update an existing device category",
        operationId = "updateCategory",
        tags = {"Device Categories"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Category ID")
        },
        requestBody = @OpenApiRequestBody(
            content = {@OpenApiContent(from = DeviceCategoryUpdateRequest.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", content = {@OpenApiContent(from = DeviceCategoryResponse.class)}),
            @OpenApiResponse(status = "400", description = "Invalid request"),
            @OpenApiResponse(status = "404", description = "Category not found")
        }
    )
    private void updateCategory(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        DeviceCategoryUpdateRequest request = ctx.bodyAsClass(DeviceCategoryUpdateRequest.class);
        DeviceCategoryResponse response = categoryService.updateCategory(id, request);
        ctx.json(response);
    }

    @OpenApi(
        path = CONTEXT_PATH + "/categories/{id}",
        methods = {HttpMethod.DELETE},
        summary = "Delete a device category",
        operationId = "deleteCategory",
        tags = {"Device Categories"},
        pathParams = {
            @OpenApiParam(name = "id", type = Long.class, description = "Category ID")
        },
        responses = {
            @OpenApiResponse(status = "204", description = "Category deleted successfully"),
            @OpenApiResponse(status = "404", description = "Category not found")
        }
    )
    private void deleteCategory(Context ctx) {
        Long id = ctx.pathParamAsClass("id", Long.class).get();
        categoryService.deleteCategory(id);
        ctx.status(204);
    }
}
