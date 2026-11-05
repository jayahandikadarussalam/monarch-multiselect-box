(function (window, $) {
    'use strict';

    function init(cell) {
        var $cell = $(cell);
        var $select = $cell.find('select.monarch-multiselect');
        if (!$select.length || $select.data('monarch-initialized') || !$.fn.chosen) {
            return;
        }
        // Use the same layout and Chosen widget as Joget's built-in Multi Select Box.
        var width = $select.attr('data-width') || '40%';
        if ($(window).width() <= 767) { width = '100%'; }
        if ($('body').hasClass('rtl')) { $select.addClass('chosen-rtl'); }
        $select.chosen({
            width: width,
            placeholder_text_multiple: $select.attr('data-placeholder'),
            search_contains: true,
            no_results_text: 'Tidak ada hasil ditemukan'
        });
        var chosen = $select.data('chosen');
        if (!chosen) {
            // Some older Chosen versions skip mobile browsers. Keep the native field usable.
            $select.parent().removeClass('ui-screen-hidden');
            $select.show();
            return;
        }
        $select.data('monarch-initialized', true);
        var $container = chosen.container;
        $container.insertAfter($select.closest('.monarch-multiselect-source'));

        var $search = chosen.search_field;
        $search.attr('aria-label', $cell.children('label.label').clone().children().remove().end().text().trim());
        $search.attr('aria-describedby', $select.attr('aria-describedby'));
        if ($select.attr('aria-invalid')) { $search.attr('aria-invalid', 'true'); }
        // The original select is hidden by Chosen, so send label clicks to its search field.
        $cell.children('label.label').on('click.monarchMultiselect', function (event) {
            if (!$select.prop('disabled')) {
                event.preventDefault();
                $search.trigger('focus');
            }
        });

        function refresh() { $select.trigger('chosen:updated'); }
        $select.on('change.monarchMultiselect jsection:show.monarchMultiselect jsection:hide.monarchMultiselect', refresh);
        if ($select.attr('data-control-field') && $.fn.dynamicOptions && !$select.prop('disabled')) {
            $select.dynamicOptions({
                controlField: $select.attr('data-control-field'),
                paramName: $select.attr('name'),
                type: 'selectbox',
                readonly: $select.attr('data-readonly'),
                nonce: $select.attr('data-nonce'),
                binderData: $select.attr('data-binder'),
                appId: $select.attr('data-app-id'),
                appVersion: $select.attr('data-app-version'),
                contextPath: $select.attr('data-context-path')
            });
            refresh();
        }
        // Native reset happens after the reset event; synchronize the displayed chips.
        $select.closest('form').on('reset.monarchMultiselect', function () {
            window.setTimeout(refresh, 0);
        });
    }

    window.MonarchMultiSelect = {init: init};
})(window, jQuery);
