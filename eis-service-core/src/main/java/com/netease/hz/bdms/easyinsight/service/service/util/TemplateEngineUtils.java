package com.netease.hz.bdms.easyinsight.service.service.util;

import com.netease.hz.bdms.easyinsight.common.exception.CommonException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * 模版引擎工具，基于freeMarker
 */
@Slf4j
public class TemplateEngineUtils {

    public static String format(String template, Map<String, Object> context) {
        try {
            Configuration cfg = new Configuration();
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Template t = new Template("templateName", new StringReader(template), cfg);
            Writer out = new StringWriter();
            t.process(context, out);
            return out.toString();
        } catch (Exception e) {
            log.error("format template error", e);
            throw new CommonException("模版格式化失败");
        }
    }
}
