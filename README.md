# Android中执行 shell 命令示例

- 应用获取 **root** 权限需要在 `AndroidManifest.xml` 中添加如下权限声明：

```xml
<uses-permission android:name="android.permission.ACCESS_SUPERUSER"/>
```

- 效果图：

![exec_wget](screenshots/exec_wget.png)

![exec_ps](screenshots/exec_ps.png)
