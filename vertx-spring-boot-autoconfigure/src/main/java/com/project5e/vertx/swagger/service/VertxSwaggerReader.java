package com.project5e.vertx.swagger.service;

import cn.hutool.core.util.TypeUtil;
import com.project5e.vertx.swagger.annotation.SwaggerPage;
import com.project5e.vertx.web.annotation.*;
import com.project5e.vertx.web.component.BaseMethod;
import com.project5e.vertx.web.service.MethodDescriptor;
import com.project5e.vertx.web.service.ProcessResult;
import com.project5e.vertx.web.service.RouterDescriptor;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.jaxrs2.SecurityParser;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class VertxSwaggerReader {

    private static final String PATH_DELIMITER = "/";
    public static final String DEFAULT_PAGE_KEY = "default";


    private final Map<String, Integer> operationIdCache = new HashMap<>();
    private final OpenAPI openAPI;

    public VertxSwaggerReader() {
        openAPI = new OpenAPI();
        openAPI.setPaths(new Paths());
        openAPI.setComponents(new Components());
    }

    public static List<String> parsePageKeys(ProcessResult processResult) {
        Set<String> pageKeys = new HashSet<>();
        for (RouterDescriptor routerDescriptor : processResult.getRouterDescriptors()) {
            Class<?> cls = routerDescriptor.getClazz();
            SwaggerPage page = ReflectionUtils.getAnnotation(cls, SwaggerPage.class);
            String pageKey = page != null ? page.value() : DEFAULT_PAGE_KEY;
            pageKeys.add(pageKey);
        }
        return pageKeys.stream().sorted().collect(Collectors.toList());
    }

    private String parsePageKey(RouterDescriptor routerDescriptor) {
        SwaggerPage page = ReflectionUtils.getAnnotation(routerDescriptor.getClazz(), SwaggerPage.class);
        return page != null ? page.value() : DEFAULT_PAGE_KEY;
    }

    /**
     * 从Vertx-spring-Web的扫描结果中读取并构建OpenAPI对象
     *
     * @param processResult vertx-spring-web扫描对象
     * @return 返回OpenAPI对象
     */
    public OpenAPI read(ProcessResult processResult, String pageKey) {
        for (RouterDescriptor routerDescriptor : processResult.getRouterDescriptors()) {
            if (parsePageKey(routerDescriptor).equals(pageKey)) {
                read(routerDescriptor);
            }
        }
        return openAPI;
    }

    private void read(RouterDescriptor routerDescriptor) {
        Class<?> cls = routerDescriptor.getClazz();
        // 整个Router被隐藏
        Hidden hidden = cls.getAnnotation(Hidden.class);
        if (hidden != null) {
            return;
        }
        // 处理API公共定义参数，公共参数通过 VertxOpenApiDefinition 类进行定义
        // handleOpenApiDefinition(cls);
        // 类上的公共注解
        RequestMapping classRequestMapping = ReflectionUtils.getAnnotation(cls, RequestMapping.class);
        String[] classPaths = classRequestMapping != null ? classRequestMapping.value() : new String[]{null};
        io.swagger.v3.oas.annotations.tags.Tag[] apiTags = ReflectionUtils.getRepeatableAnnotationsArray(cls, io.swagger.v3.oas.annotations.tags.Tag.class);
        io.swagger.v3.oas.annotations.servers.Server[] apiServers = ReflectionUtils.getRepeatableAnnotationsArray(cls, io.swagger.v3.oas.annotations.servers.Server.class);
        io.swagger.v3.oas.annotations.responses.ApiResponse[] apiResponses = ReflectionUtils.getRepeatableAnnotationsArray(cls, io.swagger.v3.oas.annotations.responses.ApiResponse.class);
        List<io.swagger.v3.oas.annotations.security.SecurityScheme> apiSecurityScheme = ReflectionUtils.getRepeatableAnnotations(cls, io.swagger.v3.oas.annotations.security.SecurityScheme.class);
        List<io.swagger.v3.oas.annotations.security.SecurityRequirement> apiSecurityRequirements = ReflectionUtils.getRepeatableAnnotations(cls, io.swagger.v3.oas.annotations.security.SecurityRequirement.class);

        // 处理类上的servers
        final List<Server> classServers = new ArrayList<>();
        if (apiServers != null) {
            AnnotationsUtils.getServers(apiServers).ifPresent(classServers::addAll);
        }
        // 处理类上的tags
        final Set<String> classTags = new LinkedHashSet<>();
        if (apiTags != null) {
            AnnotationsUtils
                .getTags(apiTags, false)
                .ifPresent(tags -> tags.stream().map(Tag::getName).forEach(classTags::add));
        } else {
            classTags.add(routerDescriptor.getClazz().getSimpleName());
        }
        for (String classTag : classTags) {
            openAPI.addTagsItem(new Tag().name(classTag).description(null));
        }
        // 处理类上的security
        if (apiSecurityScheme != null) {
            Components components = openAPI.getComponents();
            for (io.swagger.v3.oas.annotations.security.SecurityScheme securitySchemeAnnotation : apiSecurityScheme) {
                Optional<SecurityParser.SecuritySchemePair> securityScheme = SecurityParser.getSecurityScheme(securitySchemeAnnotation);
                if (securityScheme.isPresent()) {
                    Map<String, SecurityScheme> securitySchemeMap = new HashMap<>();
                    if (StringUtils.isNotBlank(securityScheme.get().key)) {
                        securitySchemeMap.put(securityScheme.get().key, securityScheme.get().securityScheme);
                        if (components.getSecuritySchemes() != null && components.getSecuritySchemes().size() != 0) {
                            components.getSecuritySchemes().putAll(securitySchemeMap);
                        } else {
                            components.setSecuritySchemes(securitySchemeMap);
                        }
                    }
                }
            }
        }
        List<SecurityRequirement> classSecurityRequirements = new ArrayList<>();
        if (apiSecurityRequirements != null) {
            Optional<List<SecurityRequirement>> requirementsObject = SecurityParser.getSecurityRequirements(
                apiSecurityRequirements.toArray(new io.swagger.v3.oas.annotations.security.SecurityRequirement[0])
            );
            if (requirementsObject.isPresent()) {
                classSecurityRequirements = requirementsObject.get();
            }
        }

        // 处理类上的responses
        final List<io.swagger.v3.oas.annotations.responses.ApiResponse> classResponses = new ArrayList<>();
        if (apiResponses != null) {
            classResponses.addAll(Arrays.asList(apiResponses));
        }
        // 正式解析
        for (MethodDescriptor methodDescriptor : routerDescriptor.getMethodDescriptors()) {
            if (isOperationHidden(methodDescriptor.getBaseMethod().getMethod())) {
                continue;
            }
            for (HttpMethod httpMethod : methodDescriptor.getHttpMethods()) {
                for (String methodPath : methodDescriptor.getPaths()) {
                    // methodPath已经包含了类上的路径，因此不再需要去除classPaths
                    if (methodPath == null) {
                        continue;
                    }
                    Operation operation = parseOperation(methodDescriptor, classServers, classTags, classSecurityRequirements, classResponses);
                    if (operation == null) {
                        continue;
                    }
                    String path = convertPath(methodPath);
                    PathItem pathItem = openAPI.getPaths().computeIfAbsent(path, k -> new PathItem());
                    fillPathItemWithOperation(pathItem, httpMethod, operation);
                }
            }
        }
    }

    private void fillPathItemWithOperation(PathItem pathItem, HttpMethod httpMethod, Operation operation) {
        switch (httpMethod) {
            case OPTIONS:
                pathItem.setOptions(operation);
                break;
            case GET:
                pathItem.setGet(operation);
                break;
            case POST:
                pathItem.setPost(operation);
                break;
            case PUT:
                pathItem.setPut(operation);
                break;
            case PATCH:
                pathItem.setPatch(operation);
                break;
            case DELETE:
                pathItem.setDelete(operation);
                break;
            case HEAD:
                pathItem.setHead(operation);
                break;
            case TRACE:
                pathItem.setTrace(operation);
                break;
            case CONNECT:
            case OTHER:
                break;
        }
    }

    /**
     * 将MethodDescriptor解析成Operation
     *
     * @param methodDescriptor 方法描述信息
     * @param classServers     类级别的servers
     * @return 返回解析完成的Operation
     */
    protected Operation parseOperation(
        MethodDescriptor methodDescriptor, List<Server> classServers, Set<String> classTags,
        List<SecurityRequirement> classSecurityRequirements,
        List<io.swagger.v3.oas.annotations.responses.ApiResponse> classResponses
    ) {
        BaseMethod baseMethod = methodDescriptor.getBaseMethod();
        Method method = baseMethod.getMethod();
        Operation operation = new Operation();

        io.swagger.v3.oas.annotations.Operation apiOperation = ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.Operation.class);
        List<io.swagger.v3.oas.annotations.servers.Server> apiServers = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.servers.Server.class);
        List<io.swagger.v3.oas.annotations.tags.Tag> apiTags = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.tags.Tag.class);
        List<io.swagger.v3.oas.annotations.responses.ApiResponse> apiResponses = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.responses.ApiResponse.class);
        ExternalDocumentation apiExternalDocumentation = ReflectionUtils.getAnnotation(method, ExternalDocumentation.class);
        List<io.swagger.v3.oas.annotations.security.SecurityRequirement> apiSecurity = ReflectionUtils.getRepeatableAnnotations(method, io.swagger.v3.oas.annotations.security.SecurityRequirement.class);

        // TODO 待加入callback：io.swagger.v3.jaxrs2.Reader.getCallbacks
        classSecurityRequirements.forEach(operation::addSecurityItem);
        if (apiSecurity != null) {
            Optional<List<SecurityRequirement>> requirementsObject = SecurityParser.getSecurityRequirements(apiSecurity.toArray(new io.swagger.v3.oas.annotations.security.SecurityRequirement[0]));
            requirementsObject.ifPresent(
                securityRequirements -> securityRequirements.stream()
                    .filter(r -> operation.getSecurity() == null || !operation.getSecurity().contains(r))
                    .forEach(operation::addSecurityItem));
        }

        // TODO 原生Operation暂只支持描述信息的设置
        if (apiOperation != null) {
            operation.setSummary(apiOperation.summary());
            operation.setDescription(apiOperation.description());
        } else {
            String methodSummary = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(method.getName()));
            operation.setSummary(methodSummary);
            operation.setDescription(methodSummary);
        }

        // operationId
        String defaultOperationId = method.getName();
        if (operationIdCache.containsKey(defaultOperationId)) {
            Integer currentRound = operationIdCache.get(defaultOperationId);
            operation.setOperationId(String.format("%s%d", defaultOperationId, currentRound));
            operationIdCache.put(defaultOperationId, currentRound + 1);
        } else {
            operation.setOperationId(defaultOperationId);
            operationIdCache.put(defaultOperationId, 0);
        }

        // servers
        if (classServers != null) {
            classServers.forEach(operation::addServersItem);
        }
        if (apiServers != null) {
            AnnotationsUtils
                .getServers(apiServers.toArray(new io.swagger.v3.oas.annotations.servers.Server[0]))
                .ifPresent(servers -> servers.forEach(operation::addServersItem));
        }
        // external docs
        AnnotationsUtils.getExternalDocumentation(apiExternalDocumentation).ifPresent(operation::setExternalDocs);
        // tags
        if (classTags != null) {
            classTags.forEach(operation::addTagsItem);
        }
        if (apiTags != null) {
            apiTags.stream()
                .filter(t -> operation.getTags() == null || (operation.getTags() != null && !operation.getTags().contains(t.name())))
                .map(io.swagger.v3.oas.annotations.tags.Tag::name)
                .forEach(operation::addTagsItem);
            AnnotationsUtils.getTags(apiTags.toArray(new io.swagger.v3.oas.annotations.tags.Tag[0]), true).ifPresent(tags -> openAPI.getTags().addAll(tags));
        }

        // parameters，参数的解析主要来自我们自定义的注解
        parseParameters(methodDescriptor).forEach(operation::addParametersItem);
        // requestBody
        operation.setRequestBody(parseRequestBody(methodDescriptor));
        // response，从返回类型中拿到响应的schema
        List<io.swagger.v3.oas.annotations.responses.ApiResponse> annotationResponses = new ArrayList<>();
        if (apiResponses != null) annotationResponses.addAll(apiResponses);
        if (classResponses != null) annotationResponses.addAll(classResponses);
        operation.setResponses(parseResponses(methodDescriptor, annotationResponses));

        return operation;
    }

    protected List<Parameter> parseParameters(MethodDescriptor methodDescriptor) {
        // TODO 待加入对swagger的Parameter注解的支持
        List<Parameter> parameters = new ArrayList<>();
        for (java.lang.reflect.Parameter parameter : methodDescriptor.getBaseMethod().getParameters()) {
            // 目前允许结果放在Promise或Future中，因此忽略参数中的Promise
            if (parameter.getType().equals(Promise.class)) {
                continue;
            }
            // method为GET时，RequestBody从query中获取数据
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null && Arrays.equals(methodDescriptor.getHttpMethods(), new HttpMethod[]{HttpMethod.GET})) {
                for (Field pField : parameter.getType().getDeclaredFields()) {
                    Schema<?> pSchema = getSchema(pField.getGenericType());
                    fillSchemaWithValidation(pSchema, pField);
                    parameters.add(new QueryParameter()
                        .name(pField.getName())
                        .schema(pSchema));
                }
            }
            RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
            if (requestHeader != null) {
                String pName = StringUtils.firstNonBlank(requestHeader.value(), parameter.getName());
                parameters.add(new HeaderParameter()
                    .name(pName)
                    .schema(getSchema(parameter.getParameterizedType()))
                    .required(requestHeader.required()));
            }
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String pName = StringUtils.firstNonBlank(requestParam.value(), parameter.getName());
                parameters.add(new QueryParameter()
                    .name(pName)
                    .schema(getSchema(parameter.getParameterizedType()))
                    .required(requestParam.required()));
            }
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String pName = StringUtils.firstNonBlank(pathVariable.value(), parameter.getName());
                parameters.add(new PathParameter()
                    .name(pName)
                    .schema(getSchema(parameter.getParameterizedType()))
                    .required(pathVariable.required()));
            }
        }
        return parameters;
    }

    protected io.swagger.v3.oas.models.parameters.RequestBody parseRequestBody(MethodDescriptor methodDescriptor) {
        // 如果是GET，则忽略RequestBody注解
        if (Arrays.equals(methodDescriptor.getHttpMethods(), new HttpMethod[]{HttpMethod.GET})) {
            return null;
        }
        BaseMethod baseMethod = methodDescriptor.getBaseMethod();
        List<java.lang.reflect.Parameter> parametersAnnotatedWithRequestBody = Arrays
            .stream(baseMethod.getParameters())
            .filter(p -> p.isAnnotationPresent(RequestBody.class))
            .collect(Collectors.toList());
        if (parametersAnnotatedWithRequestBody.size() == 0) {
            return null;
        }
        // 当出现多个RequestBody注解时，只取第一个，其它直接忽略
        if (parametersAnnotatedWithRequestBody.size() > 1) {
            log.warn("{} has more than one io.vertx.webpro.core.annotation.RequestBody annotation, ignore others except the first one"
                , baseMethod.getMethod().getName());
        }
        java.lang.reflect.Parameter parameterAnnotatedWithRequestBody = parametersAnnotatedWithRequestBody.get(0);
        Schema<?> schema = null;
        RequestBody requestBodyAnnotation = parameterAnnotatedWithRequestBody.getAnnotation(RequestBody.class);
        if (requestBodyAnnotation != null) {
            schema = getSchema(parameterAnnotatedWithRequestBody.getType());
        }

        io.swagger.v3.oas.models.parameters.RequestBody requestBody = new io.swagger.v3.oas.models.parameters.RequestBody();
        requestBody.content(new Content().addMediaType("application/json", new MediaType().schema(schema)));

        return requestBody;
    }

    /**
     * 根据方法的返回值和response注解进行解析
     *
     * @param methodDescriptor 方法描述信息
     * @param apiResponses     注解的ApiResponse信息
     * @return 返回解析完成的ApiResponse
     */
    protected ApiResponses parseResponses(
        MethodDescriptor methodDescriptor,
        List<io.swagger.v3.oas.annotations.responses.ApiResponse> apiResponses
    ) {
        ApiResponses apiResponsesObject = new ApiResponses();

        BaseMethod baseMethod = methodDescriptor.getBaseMethod();

        // 返回的具体类型，已在baseMethod中提取出来，无论是Promise参数还是Future，因此这里就不用管了。
        Schema<?> schema = getSchema(baseMethod.getActualType());

        MediaType methodReturnMediaType = new MediaType().schema(schema);
        ApiResponse methodResponse = new ApiResponse();
        if (schema != null) {
            // TODO 处理更多种返回类型
            final String mediaType;
            if (TypeUtil.getClass(baseMethod.getActualType()).equals(String.class)) {
                mediaType = "text/plain";
            } else {
                mediaType = "application/json";
            }
            methodResponse.setContent(new Content().addMediaType(mediaType, methodReturnMediaType));
        }
        apiResponsesObject.addApiResponse("200", methodResponse);

        // 额外的注解
        for (io.swagger.v3.oas.annotations.responses.ApiResponse response : apiResponses) {
            ApiResponse apiResponseObject = new ApiResponse();
            if (StringUtils.isNotBlank(response.ref())) {
                apiResponseObject.set$ref(response.ref());
                if (StringUtils.isNotBlank(response.responseCode())) {
                    apiResponsesObject.addApiResponse(response.responseCode(), apiResponseObject);
                } else {
                    apiResponsesObject._default(apiResponseObject);
                }
                continue;
            }
            if (StringUtils.isNotBlank(response.description())) {
                apiResponseObject.setDescription(response.description());
            }
            AnnotationsUtils
                .getContent(response.content(), new String[0], new String[0], null, openAPI.getComponents(), null)
                .ifPresent(apiResponseObject::content);
            AnnotationsUtils.getHeaders(response.headers(), null)
                .ifPresent(apiResponseObject::headers);
            if (StringUtils.isNotBlank(apiResponseObject.getDescription()) || apiResponseObject.getContent() != null || apiResponseObject.getHeaders() != null) {
                Map<String, Link> links = AnnotationsUtils.getLinks(response.links());
                if (links.size() > 0) {
                    apiResponseObject.setLinks(links);
                }
                if (StringUtils.isNotBlank(response.responseCode())) {
                    apiResponsesObject.addApiResponse(response.responseCode(), apiResponseObject);
                } else {
                    apiResponsesObject._default(apiResponseObject);
                }
            }
        }

        return apiResponsesObject;
    }

    @SuppressWarnings({"rawtypes"})
    private Schema getSchema(Type type) {
        final Schema schema;

        final Type clazz;
        final Type[] actualTypeArguments;

        if (type instanceof ParameterizedType) {
            clazz = ((ParameterizedType) type).getRawType();
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        } else {
            clazz = type;
            actualTypeArguments = new Type[]{};
        }

        // 如下几个类型ModelConverters处理有问题，这里单独处理
        if (clazz == JsonObject.class) {
            schema = new ObjectSchema().description("我只能告诉你，这是一个对象，但对象内部结构未知");
        } else if (clazz == JsonArray.class) {
            schema = new ArraySchema().description("我只能告诉你，这是一个数组，但数组内部结构未知");
        } else if (clazz == Void.class) {
            schema = null;
        } else if (clazz == List.class) {
            schema = new ArraySchema().items(getSchema(actualTypeArguments[0]));
        } else {
            // ModelConverter逻辑过于复杂，考虑自己写一个符合自己的
            ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(new AnnotatedType(type));
            schema = resolvedSchema.schema;
            resolvedSchema.referencedSchemas.forEach((key, item) -> openAPI.getComponents().addSchemas(key, item));
        }

        return schema;
    }

    protected String getPath(String classPath, String methodPath) {
        return (classPath == null ? "" : classPath.trim())
            + (methodPath == null ? "" : methodPath.trim());
    }

    /**
     * 操作路径是否需要被忽略：配置中有显式指定才忽略
     *
     * @param operationPath operation的路径
     * @param config        配置信息
     * @return true or false
     */
    protected boolean isOperationPathIgnored(String operationPath, OpenAPIConfiguration config) {
        if (config.getIgnoredRoutes() == null) {
            return false;
        }
        for (String item : config.getIgnoredRoutes()) {
            final int length = item.length();
            if (operationPath.startsWith(item) &&
                (operationPath.length() == length || operationPath.startsWith(PATH_DELIMITER, length))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Operation为hidden属性和Hidden注解时都会隐藏
     *
     * @param method 方法
     * @return true or false
     */
    protected boolean isOperationHidden(Method method) {
        io.swagger.v3.oas.annotations.Operation apiOperation = ReflectionUtils.getAnnotation(method, io.swagger.v3.oas.annotations.Operation.class);
        if (apiOperation != null && apiOperation.hidden()) {
            return true;
        }
        Hidden hidden = method.getAnnotation(Hidden.class);
        return hidden != null;
    }

    @SuppressWarnings("unused")
    private void handleOpenApiDefinition(Class<?> cls) {
        io.swagger.v3.oas.annotations.OpenAPIDefinition openAPIDefinition = ReflectionUtils.getAnnotation(cls, io.swagger.v3.oas.annotations.OpenAPIDefinition.class);
        if (openAPIDefinition != null) {
            // info
            AnnotationsUtils.getInfo(openAPIDefinition.info()).ifPresent(openAPI::setInfo);

            // OpenApiDefinition external docs
            AnnotationsUtils
                .getExternalDocumentation(openAPIDefinition.externalDocs())
                .ifPresent(openAPI::setExternalDocs);

            // OpenApiDefinition tags
            AnnotationsUtils
                .getTags(openAPIDefinition.tags(), false)
                .ifPresent(tags -> openAPI.getTags().addAll(tags));

            // OpenApiDefinition servers
            AnnotationsUtils.getServers(openAPIDefinition.servers()).ifPresent(openAPI::setServers);

            // OpenApiDefinition extensions
            if (openAPIDefinition.extensions().length > 0) {
                openAPI.setExtensions(AnnotationsUtils
                    .getExtensions(openAPIDefinition.extensions()));
            }
        }
    }

    protected void fillSchemaWithValidation(Schema<?> schema, AnnotatedElement element) {
        NotNull notNull = element.getAnnotation(NotNull.class);
        schema.nullable(notNull == null);
        Pattern pattern = element.getAnnotation(Pattern.class);
        if (pattern != null) {
            schema.pattern(pattern.regexp());
        }
        Size size = element.getAnnotation(Size.class);
        if (size != null) {
            schema.minLength(size.min());
            schema.maxLength(size.max());
        }
        Min min = element.getAnnotation(Min.class);
        if (min != null) {
            schema.minimum(BigDecimal.valueOf(min.value()));
        }
        Max max = element.getAnnotation(Max.class);
        if (max != null) {
            schema.maximum(BigDecimal.valueOf(max.value()));
        }
        DecimalMin decimalMin = element.getAnnotation(DecimalMin.class);
        if (decimalMin != null) {
            schema.minimum(BigDecimal.valueOf(Long.parseLong(decimalMin.value())));
        }
        DecimalMax decimalMax = element.getAnnotation(DecimalMax.class);
        if (decimalMax != null) {
            schema.maximum(BigDecimal.valueOf(Long.parseLong(decimalMax.value())));
        }
        NotBlank notBlank = element.getAnnotation(NotBlank.class);
        NotEmpty notEmpty = element.getAnnotation(NotEmpty.class);
        if (notBlank != null || notEmpty != null) {
            schema.nullable(false);
            schema.minLength(1);
        }
        Email email = element.getAnnotation(Email.class);
        if (email != null) {
            schema.format("email");
        }
    }

    /**
     * /:id => /{id}
     *
     * @param path 路径
     * @return 转换之后的路径
     */
    private String convertPath(String path) {
        String[] split = path.split("/");
        return Arrays.stream(split).map(s -> {
            if (s.startsWith(":")) {
                return "{" + s.substring(1) + "}";
            } else {
                return s;
            }
        }).collect(Collectors.joining("/"));
    }
}
