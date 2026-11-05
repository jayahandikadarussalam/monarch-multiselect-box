package id.co.itasoft.monarch.plugin;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonarchTemplateTest {
    private String render(boolean readonly, boolean labelOnly, boolean empty, boolean error) throws Exception {
        Configuration configuration = new Configuration();
        configuration.setClassForTemplateLoading(getClass(), "/templates");
        Template template = configuration.getTemplate("monarchMultiSelectBox.ftl", "UTF-8");
        Map<String, Object> properties = new HashMap<>();
        properties.put("label", "Team <script>alert(1)</script>");
        properties.put("readonly", String.valueOf(readonly));
        properties.put("readonlyLabel", String.valueOf(labelOnly));
        properties.put("helpText", "Choose <two>");
        Map<String, Object> model = new HashMap<>();
        model.put("element", Collections.singletonMap("properties", properties));
        model.put("request", Collections.singletonMap("contextPath", "/jw"));
        model.put("inputId", "monarch-test-1");
        model.put("elementParamName", "team");
        model.put("values", empty ? Collections.emptyList() : Arrays.asList("a\"b"));
        Map<String, String> option = new HashMap<>();
        option.put("value", "a\"b");
        option.put("label", "<img src=x onerror=alert(1)>");
        model.put("options", empty ? Collections.emptyList() : Arrays.asList(option));
        if (error) model.put("error", "Required <field>");
        StringWriter result = new StringWriter();
        template.process(model, result);
        return result.toString();
    }

    @Test public void escapesContentAndPreservesSelectedValue() throws Exception {
        String html = render(false, false, false, false);
        assertTrue(html.contains("value=\"a&quot;b\" selected=\"selected\""));
        assertTrue(html.contains("&lt;img"));
        assertFalse(html.contains("<img"));
        assertFalse(html.contains("<script>alert"));
        assertTrue(html.contains("for=\"monarch-test-1\""));
        assertTrue(html.contains("grouping=\"\""));
        assertFalse(html.contains("cdnjs.cloudflare.com"));
    }

    @Test public void loadsNativeJogetAssetsWithTheCorrectResourcePath() throws Exception {
        String html = render(false, false, false, false);
        assertTrue(html.contains("/jw/plugin/id.co.itasoft.monarch.plugin.MonarchMultiSelectBox\""));
        assertFalse(html.contains("MonarchMultiSelectBox/resources"));
        assertTrue(html.contains("/js/chosen/chosen.jquery.js"));
        assertFalse(html.contains("/js/select2.min.js"));
        assertTrue(html.contains("document.getElementById('monarch-test-1-cell')"));
        assertFalse(html.contains("})(document.currentScript)"));
        assertTrue(html.contains("ui-screen-hidden monarch-multiselect-source"));
        assertTrue(html.contains("data-role=\"none\""));
    }

    @Test public void readonlyKeepsSubmittedValue() throws Exception {
        String html = render(true, false, false, false);
        assertTrue(html.contains("disabled=\"disabled\""));
        assertTrue(html.contains("type=\"hidden\" name=\"team\" value=\"a&quot;b\""));
    }

    @Test public void readonlyLabelRendersChipsWithoutSelect() throws Exception {
        String html = render(true, true, false, false);
        assertFalse(html.contains("<select"));
        assertTrue(html.contains("readonly_label"));
        assertTrue(html.contains("type=\"hidden\""));
    }

    @Test public void emptyOptionsAndErrorRenderSafely() throws Exception {
        String html = render(false, false, true, true);
        assertTrue(html.contains("aria-invalid=\"true\""));
        assertTrue(html.contains("Required &lt;field&gt;"));
        assertTrue(render(true, true, true, false).contains("Tidak ada pilihan"));
    }
}
