package com.github.zxkane.aliyunfc.demo

import com.alicloud.openservices.tablestore.SyncClient
import com.alicloud.openservices.tablestore.model.*
import com.aliyun.fc.runtime.Context
import com.aliyun.fc.runtime.FunctionInitializer
import com.aliyun.fc.runtime.PojoRequestHandler
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.random.Random

data class Event(
    @JsonProperty("triggerTime") val triggerTime: String,
    @JsonProperty("triggerName") val triggerName: String,
    @JsonProperty("payload") val payload: String
) {
    constructor() : this("", "", "")
}

class WeatherFetch : PojoRequestHandler<Event, Unit>, FunctionInitializer {

    lateinit var syncClient: SyncClient

    override fun initialize(context: Context) {
        syncClient = SyncClient(
            System.getenv(DTS_ENDPOINT), context.executionCredentials.accessKeyId,
            context.executionCredentials.accessKeySecret, System.getenv(DTS_INSTANCE_NAME),
            context.executionCredentials.securityToken
        )
    }

    override fun handleRequest(event: Event, context: Context) {
        val logger = context.logger
        logger.debug("Event $event is triggered.")

        val primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder()
        primaryKeyBuilder.addPrimaryKeyColumn("city", PrimaryKeyValue.fromString("beijing"))
        val primaryKey = primaryKeyBuilder.build()
        val rowPutChange = RowPutChange("weather", primaryKey)
        rowPutChange.addColumn("temperature", ColumnValue.fromDouble(Random.nextDouble(-10.0, 10.0)))
        rowPutChange.addColumn("lastUpdated", ColumnValue.fromString(event.triggerTime))
        syncClient.putRow(PutRowRequest(rowPutChange))
    }
}
