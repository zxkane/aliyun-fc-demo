package com.github.zxkane.aliyunfc.demo

import com.alicloud.openservices.tablestore.SyncClient
import com.alicloud.openservices.tablestore.model.GetRowRequest
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue
import com.alicloud.openservices.tablestore.model.SingleRowQueryCriteria
import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionInitializer
import com.aliyun.fc.runtime.PojoRequestHandler
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.zxkane.aliyun.fc.APIRequest
import com.github.zxkane.aliyun.fc.APIResponse
import org.apache.commons.codec.binary.Base64

const val QUERY_PARAMETER_CITY = "city"

const val DTS_ENDPOINT = "DTS_ENDPOINT"
const val DTS_ACCESS_KEY = "DTS_ACCESS_KEY"
const val DTS_KEY_SECRET = "DTS_KEY_SECRET"
const val DTS_INSTANCE_NAME = "DTS_INSTANCE_NAME"

const val STATUS_CODE = 200

data class WeatherQuery(
    @JsonProperty(required = true) val city: String
)

data class WeatherQueryResponse(
    val city: String,
    val temperature: Double
)

class Weather : PojoRequestHandler<APIRequest, APIResponse>, FunctionInitializer {

    lateinit var objectMapper: ObjectMapper
    lateinit var syncClient: SyncClient

    override fun initialize(context: Context) {
        objectMapper = ObjectMapper().registerModules(JavaTimeModule()).registerKotlinModule()
        objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
        syncClient = SyncClient(
            System.getenv(DTS_ENDPOINT), context.executionCredentials.accessKeyId,
            context.executionCredentials.accessKeySecret, System.getenv(DTS_INSTANCE_NAME),
            context.executionCredentials.securityToken
        )
    }

    override fun handleRequest(request: APIRequest, context: Context): APIResponse {
        val logger = context.logger
        logger.debug("Method request is $request")

        // 构造主键
        val primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
        primaryKeyBuilder.addPrimaryKeyColumn("city", PrimaryKeyValue.fromString(request.queryParameters.get(
            QUERY_PARAMETER_CITY)))
        val primaryKey = primaryKeyBuilder.build()

        // 读一行
        val criteria = SingleRowQueryCriteria("weather", primaryKey)
        // 设置读取最新版本
        criteria.maxVersions = 1
        val getRowResponse = syncClient.getRow(GetRowRequest(criteria))
        val row = getRowResponse.getRow()

        val response = WeatherQueryResponse(request.queryParameters.get(QUERY_PARAMETER_CITY)!!,
            row?.getColumn("temperature")?.get(0)?.let { it -> it.value.asDouble() } ?: Double.NaN)

        logger.debug("Method response is $response.")

        return APIResponse(
            Base64.encodeBase64String(objectMapper.writeValueAsString(response).toByteArray()),
            mapOf("content-eventType" to "application/json"), true, STATUS_CODE
        )
    }
}
