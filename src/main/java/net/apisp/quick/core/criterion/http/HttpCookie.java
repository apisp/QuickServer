package net.apisp.quick.core.criterion.http;

import net.apisp.quick.annotation.Description;

public interface HttpCookie {

    @Description("获取Cookie键")
    String key();

    @Description("获取Cookie值")
    String value();

    @Description("获取Cookie路径")
    String path();

    @Description("获取Cookie过期时间")
    long expires();

    @Description("设置Cookie路径")
    void path(String path);

    @Description("设置Cookie过期时间")
    void expires(long expires);
}
