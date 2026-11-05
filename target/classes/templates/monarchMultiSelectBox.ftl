<#assign readonly = (element.properties.readonly!'') == 'true'>
<#assign labelOnly = readonly && (element.properties.readonlyLabel!'') == 'true'>
<#assign placeholder = element.properties.placeholder!'Pilih opsi...'>
<#if !placeholder?has_content><#assign placeholder = 'Pilih opsi...'></#if>
<div id="${inputId?html}-cell" class="form-cell monarch-multiselect-cell<#if error??> monarch-multiselect-invalid</#if>" ${elementMetaData!}
     data-assets="${request.contextPath?html}/plugin/id.co.itasoft.monarch.plugin.MonarchMultiSelectBox"
     data-context-path="${request.contextPath?html}">
    <label class="label" <#if !labelOnly>for="${inputId?html}"</#if> field-tooltip="${elementParamName!?html}">
        ${element.properties.label!?html}
        <span class="form-cell-validator">${decoration!}</span>
        <#if error??><span id="${inputId?html}-error" class="form-error-message" role="alert">${error?html}</span></#if>
    </label>
    <#if labelOnly>
        <div class="form-cell-value">
            <#assign hasSelection = false>
            <#list options as option>
                <#if values?seq_contains(option.value!'') && (option.value!'')?has_content>
                    <label class="readonly_label"><span>${option.label!?html}</span></label>
                    <#assign hasSelection = true>
                </#if>
            </#list>
            <#if !hasSelection><span class="monarch-multiselect-empty">Tidak ada pilihan</span></#if>
        </div>
        <div style="clear:both;"></div>
    <#else>
        <#-- Match Joget's Multi Select Box structure so theme layout and Chosen styles apply. -->
        <div class="ui-screen-hidden monarch-multiselect-source">
            <select id="${inputId?html}" name="${elementParamName!?html}" multiple="multiple"
                    class="monarch-multiselect<#if error??> form-error-cell</#if>" style="display:none"
                    data-role="none" data-native-menu="true"
                    data-placeholder="${placeholder?html}" data-width="${element.properties.width!?html}"
                    data-control-field="${element.properties.controlFieldParamName!?html}"
                    data-readonly="${element.properties.readonly!?html}"
                    data-nonce="${element.properties.nonce!?html}"
                    data-binder="${element.properties.binderData!?html}"
                    data-app-id="${element.properties.appId!?html}"
                    data-app-version="${element.properties.appVersion!?html}"
                    data-context-path="${request.contextPath?html}"
                    <#if readonly>disabled="disabled"</#if>
                    <#if error??>aria-invalid="true"</#if>
                    aria-describedby="${inputId?html}-help<#if error??> ${inputId?html}-error</#if>">
                <#list options as option>
                    <#if (option.value!'')?has_content>
                        <option value="${option.value!?html}" <#if values?seq_contains(option.value!'')>selected="selected"</#if>
                                grouping="${option.grouping!?html}">${option.label!?html}</option>
                    </#if>
                </#list>
            </select>
        </div>
    </#if>
    <#if readonly>
        <#list values as value>
            <input type="hidden" name="${elementParamName!?html}" value="${value?html}" />
        </#list>
    </#if>
    <div id="${inputId?html}-help" class="form-cell-value monarch-multiselect-help">${element.properties.helpText!?html}</div>
    <#if !labelOnly>
        <script>
            jQuery(function ($) {
                // Joget may evaluate this script via AJAX, where document.currentScript is null.
                var cell = document.getElementById('${inputId?js_string}-cell');
                if (!cell) { return; }
                var base = cell.getAttribute('data-assets');
                var contextPath = cell.getAttribute('data-context-path');
                function stylesheet(id, url) {
                    if (!document.getElementById(id) && !$('link[rel="stylesheet"]').filter(function () {
                        return this.href.split('?')[0] === new URL(url, document.baseURI).href.split('?')[0];
                    }).length) {
                        $('<link>', {id: id, rel: 'stylesheet', href: url}).appendTo('head');
                    }
                }
                stylesheet('monarch-chosen-css', contextPath + '/js/chosen/chosen.css');
                stylesheet('monarch-multiselect-css-802', base + '/css/monarch-multiselect.css?v=8.0.2');
                if (!window.monarchMultiSelectReady802) {
                    var load = $.Deferred();
                    window.monarchMultiSelectReady802 = load.promise();
                    function loadComponent() {
                        $.ajax({url: base + '/js/monarch-multiselect.js?v=8.0.2', dataType: 'script', cache: true})
                            .done(function () { load.resolve(); }).fail(function () { load.reject(); });
                    }
                    if ($.fn.chosen) {
                        loadComponent();
                    } else {
                        $.ajax({url: contextPath + '/js/chosen/chosen.jquery.js', dataType: 'script', cache: true})
                            .done(loadComponent).fail(function () { load.reject(); });
                    }
                    load.fail(function () { window.monarchMultiSelectReady802 = null; });
                }
                function fallback() {
                    $(cell).children('.monarch-multiselect-source').removeClass('ui-screen-hidden');
                    $(cell).find('select.monarch-multiselect').show();
                    if (window.console) { console.error('Monarch Multiselect: gagal memuat aset Chosen dari Joget.'); }
                }
                var ready = window.monarchMultiSelectReady802;
                if (ready) {
                    ready.done(function () { window.MonarchMultiSelect.init(cell); }).fail(fallback);
                } else {
                    fallback();
                }
            });
        </script>
    </#if>
</div>
