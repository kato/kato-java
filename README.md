## 异常处理定义

| HTTP状态码 | 描述                | 异常类名                                                                                                                                                                                                                                                              |
|---------|-------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 200     | 正常业务流程            | [KatoBusinessException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FKatoBusinessException.java)                                                                                                                                    |
| 400     | 请求验证不通过，参数校验失败    | [BadRequestException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FBadRequestException.java)                                                                                                                                        |
| 401     | 未授权               | [KatoAuthenticationException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FKatoAuthenticationException.java)                                                                                                                        |
| 403     | 拒绝访问              | [KatoAccessDeniedException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FKatoAccessDeniedException.java)                                                                                                                            |
| 500     | 服务器内部报错,消息提醒，特定业务 | [KatoUndeclaredException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FKatoUndeclaredException.java) [KatoBusinessException.java](common%2Fsrc%2Fmain%2Fjava%2Fme%2Fdanwi%2Fkato%2Fcommon%2Fexception%2FKatoBusinessException.java) |

##     


