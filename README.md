阿里云函数计算demo[Presentation](https://docs.google.com/presentation/d/1HlHT8NZ19HODlxcmSJNz5wEc4CL3KbPd1eo_JNww-6o/edit?usp=sharing)

函数使用Kotlin开发，使用函数计算的Java8运行环境，集成了SLS日志服务，VPC配置以及Table Store表格存储。

demo集成了以下触发器:
- API Gateway
- Timer

#### 如何部署
- 通过[sigil](https://github.com/gliderlabs/sigil)将`template.tmpl`中变量替换为实际的配置，具体使用方式参考`.gitlab-ci.yml`
- 通过官方工具[fun](https://github.com/aliyun/fun)部署

```shell
./gradlw build
fun deploy
```
