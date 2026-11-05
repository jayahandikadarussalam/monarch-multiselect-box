package id.co.itasoft.monarch.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormAjaxOptionsBinder;
import org.joget.apps.form.model.FormAjaxOptionsElement;
import org.joget.apps.form.model.FormBinder;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormLoadBinder;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.apps.userview.model.PwaOfflineValidation;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.model.PropertyEditable;

public class MonarchMultiSelectBox extends Element implements FormBuilderPaletteElement, FormAjaxOptionsElement, PwaOfflineValidation {
    private Element controlElement;
    private static final String PLUGIN_NAME = "Shadow Multiselect Box";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return "8.0.2";
    }

    @Override
    public String getDescription() {
        return "Searchable multiple selection field using Joget native Chosen styling with select2 library. This plugin is a modified version of the Joget native MultiSelectBox plugin with additional features and improvements.";
    }

    /**
     * Returns the option key=value pairs for this select box.
     * @param formData
     * @return
     */
    public Collection<Map> getOptionMap(FormData formData) {
        return FormUtil.getElementPropertyOptionsMap(this, formData);
    }

    @Override
    public FormData formatDataForValidation(FormData formData) {
        String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
        String paramName = FormUtil.getElementParameterName(this);

        if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
            formData.addRequestParameterValues(paramName, new String[]{""});
        } else if (paramValues != null && FormUtil.isFormSubmitted(this, formData)) {
            //check & remove invalid data from values
            Collection<String> newValues = new ArrayList<String>();
            Set<String> allValues = new HashSet<String>();
            if (FormUtil.isAjaxOptionsSupported(this, formData)) {
                FormAjaxOptionsBinder ab = (FormAjaxOptionsBinder) getOptionsBinder();
                Element control = getControlElement(formData);
                String[] controlValues = control == null ? null : FormUtil.getRequestParameterValues(control, formData);
                if (controlValues == null) {
                    controlValues = new String[0];
                }
                if (controlValues.length == 1 && controlValues[0] != null && controlValues[0].contains(";")) {
                    controlValues = controlValues[0].split(";"); //to consistent the behaviour with FormUtil.getAjaxOptionsBinderData line 2013
                }
                FormRowSet rowSet = ab.loadAjaxOptions(controlValues);
                if (rowSet != null) {
                    formData.setOptionsBinderData(getOptionsBinder(), rowSet);
                    for (FormRow r : rowSet) {
                        allValues.add(r.getProperty(FormUtil.PROPERTY_VALUE));
                    }
                }
            } else {
                Collection<Map> optionMap = FormUtil.getElementPropertyOptionsMap(this, formData);

                //for other child implementation which does not using options binder & option grid, do nothing
                if (optionMap == null || optionMap.isEmpty()) {
                    return formData;
                }
                for (Map option : optionMap) {
                    if (option.get(FormUtil.PROPERTY_VALUE) != null) {
                        allValues.add(option.get(FormUtil.PROPERTY_VALUE).toString());
                    }
                }
            }
            for (String pv : paramValues) {
                if (allValues.contains(pv)) {
                    newValues.add(pv);
                }
            }

            if (newValues.isEmpty()) {
                newValues.add("");
            }

            formData.addRequestParameterValues(paramName, newValues.toArray(new String[0]));
        }
        return formData;
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        FormRowSet rowSet = null;

        // get value
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id != null) {
            String[] values = FormUtil.getElementPropertyValues(this, formData);
            // A submitted empty multi-select must clear any previously stored value.
            String[] paramValues = FormUtil.getRequestParameterValues(this, formData);
            if ((paramValues == null || paramValues.length == 0) && FormUtil.isFormSubmitted(this, formData)) {
                values = new String[]{""};
            }
            if (values != null && values.length > 0) {
                // formulate values
                String delimitedValue = FormUtil.generateElementPropertyValues(values);

                // set value into Properties and FormRowSet object
                FormRow result = new FormRow();
                result.setProperty(id, delimitedValue);
                rowSet = new FormRowSet();
                rowSet.add(result);
            }
        }

        return rowSet;
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "monarchMultiSelectBox.ftl";

        dynamicOptions(formData);

        // set value
        String[] valueArray = FormUtil.getElementPropertyValues(this, formData);
        List<String> values = valueArray == null ? Collections.emptyList() : Arrays.asList(valueArray);
        dataModel.put("values", values);

        // set options
        Collection<Map> optionMap = getOptionMap(formData);
        dataModel.put("options", optionMap == null ? Collections.emptyList() : optionMap);
        dataModel.put("inputId", "monarch-multiselect-" + UUID.randomUUID());

        return FormUtil.generateElementHtml(this, formData, template, dataModel);
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>Shadow Multiselect Box</label>"
                + "<div class='chosen-container chosen-container-multi' style='width:40%'>"
                + "<ul class='chosen-choices'><li class='search-choice'><span>Pilihan 1</span>"
                + "<a class='search-choice-close'></a></li></ul></div>";
    }

    @Override
    public String getLabel() {
        return PLUGIN_NAME;
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/monarchMultiSelectBox.json", null, true, "messages/form/monarchMultiSelectBox");
    }

    @Override
    public String getFormBuilderCategory() {
        return "<span><i class=\"fas fa-crown\"></i> Monarch </span>";
    }

    @Override
    public int getFormBuilderPosition() {
        return 300;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-caret-square-down\"></i>";
    }

    protected void dynamicOptions(FormData formData) {
        if (getControlElement(formData) != null) {
            setProperty("controlFieldParamName", FormUtil.getElementParameterName(getControlElement(formData)));

            FormUtil.setAjaxOptionsElementProperties(this, formData);
        }
    }

    public Element getControlElement(FormData formData) {
        if (controlElement == null) {
            if (getPropertyString("controlField") != null && !getPropertyString("controlField").isEmpty()) {
                Form form = FormUtil.findRootForm(this);
                controlElement = FormUtil.findElement(getPropertyString("controlField"), form, formData);
            }
        }
        return controlElement;
    }

    @Override
    public Map<WARNING_TYPE, String[]> validation() {
        Object binderData = getProperty(FormBinder.FORM_OPTIONS_BINDER);
        if (binderData != null && binderData instanceof Map) {
            Map bdMap = (Map) binderData;
            if (bdMap != null && bdMap.containsKey("className") && !bdMap.get("className").toString().isEmpty()) {
                PluginManager pluginManager = (PluginManager) AppUtil.getApplicationContext().getBean("pluginManager");
                FormLoadBinder binder = (FormLoadBinder) pluginManager.getPlugin(bdMap.get("className").toString());

                if (binder != null) {
                    Map bdProps = (Map) bdMap.get("properties");
                    ((PropertyEditable) binder).setProperties(bdProps);

                    if (binder instanceof FormAjaxOptionsBinder && ((FormAjaxOptionsBinder) binder).useAjax()) {
                        Map<WARNING_TYPE, String[]> warning = new HashMap<WARNING_TYPE, String[]>();
                        warning.put(WARNING_TYPE.NOT_SUPPORTED, new String[]{ResourceBundleUtil.getMessage("pwa.AjaxOptionsNotSupported")});
                        return warning;
                    }
                }
            }
        }
        return null;
    }
}


