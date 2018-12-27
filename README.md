阿里云函数计算demo

函数使用Kotlin开发，使用函数计算的Java8运行环境，集成了SLS日志服务，VPC配置以及Table Store表格存储。

demo集成了以下触发器:
- API Gateway
- Timer

#### 如何部署
- 如需开启函数日志，需提前手动创建对应的SLS日志project以及log store
- 通过[sigil](https://github.com/gliderlabs/sigil)将`template.tmpl`中变量替换为实际的配置，具体使用方式参考`.gitlab-ci.yml`
- 通过官方工具[fun](https://github.com/aliyun/fun)部署

```shell
./gradlw build
fun deploy
```