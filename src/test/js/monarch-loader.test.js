const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const template = fs.readFileSync(path.join(__dirname, '../../main/resources/templates/monarchMultiSelectBox.ftl'), 'utf8');
const bootstrap = template.match(/<script>([\s\S]*?)<\/script>/)[1];

function deferred() {
    let state = 'pending';
    const done = [];
    const fail = [];
    const promise = {
        done(callback) { done.push(callback); if (state === 'resolved') callback(); return promise; },
        fail(callback) { fail.push(callback); if (state === 'rejected') callback(); return promise; }
    };
    return {
        ...promise,
        promise: () => promise,
        resolve() { state = 'resolved'; done.forEach(callback => callback()); },
        reject() { state = 'rejected'; fail.forEach(callback => callback()); }
    };
}

function environment(chosenAvailable = false) {
    const cells = new Map();
    const initialized = [];
    const requests = [];
    const links = [];
    const errors = [];
    const fallbackFields = [];
    const document = {
        currentScript: null, // jQuery's AJAX/globalEval path has no current script node.
        baseURI: 'https://joget.example/jw/form',
        getElementById(id) { return cells.get(id) || null; }
    };
    function $(item, attrs) {
        if (typeof item === 'function') { item($); return; }
        if (item === 'link[rel="stylesheet"]') { return {filter: () => ({length: 0})}; }
        if (item === '<link>') { return {appendTo() { links.push(attrs.href); }}; }
        return {
            children() { return this; },
            find() { return this; },
            removeClass() { fallbackFields.push(item.id); return this; },
            show() { return this; }
        };
    }
    $.fn = chosenAvailable ? {chosen() {}} : {};
    $.Deferred = deferred;
    $.ajax = options => {
        const result = deferred();
        requests.push({url: options.url, complete: result.resolve, fail: result.reject});
        return result.promise();
    };
    const window = {console: {error: message => errors.push(message)}};
    const context = vm.createContext({window, document, jQuery: $, URL, console: window.console});
    function render(id) {
        const cell = {id: id + '-cell', getAttribute: name => name === 'data-assets'
            ? '/jw/plugin/id.co.itasoft.monarch.plugin.MonarchMultiSelectBox' : '/jw'};
        cells.set(cell.id, cell);
        vm.runInContext(bootstrap.replaceAll('${inputId?js_string}', id), context);
    }
    function componentReady() {
        window.MonarchMultiSelect = {init: cell => initialized.push(cell.id)};
        requests.at(-1).complete();
    }
    return {render, requests, initialized, links, componentReady, window, fallbackFields, errors};
}

test('AJAX-rendered fields initialize without document.currentScript and share pending asset requests', () => {
    const env = environment();
    env.render('first');
    env.render('second');
    assert.equal(env.requests.length, 1);
    assert.equal(env.requests[0].url, '/jw/js/chosen/chosen.jquery.js');
    env.requests[0].complete();
    assert.equal(env.requests.length, 2);
    assert.equal(env.requests[1].url, '/jw/plugin/id.co.itasoft.monarch.plugin.MonarchMultiSelectBox/js/monarch-multiselect.js?v=8.0.2');
    env.componentReady();
    assert.deepEqual(env.initialized, ['first-cell', 'second-cell']);
    env.render('third');
    assert.equal(env.requests.length, 2);
    assert.deepEqual(env.initialized, ['first-cell', 'second-cell', 'third-cell']);
    assert.ok(env.links.includes('/jw/js/chosen/chosen.css'));
    // PluginResourceServlet adds /resources itself; verify the requested plugin files exist.
    for (const url of [...env.links, env.requests[1].url].filter(url => url.includes('/plugin/'))) {
        const relative = url.split('MonarchMultiSelectBox/')[1].split('?')[0];
        assert.ok(fs.existsSync(path.join(__dirname, '../../main/resources/resources', relative)), relative);
        assert.ok(!relative.startsWith('resources/'));
    }
});

test('uses the Chosen library already provided by Joget', () => {
    const env = environment(true);
    env.render('first');
    assert.equal(env.requests.length, 1);
    assert.ok(env.requests[0].url.includes('/js/monarch-multiselect.js'));
    env.componentReady();
    assert.deepEqual(env.initialized, ['first-cell']);
});

test('asset failures expose a usable fallback and allow the next field to retry loading', () => {
    const env = environment();
    env.render('first');
    env.requests[0].fail();
    assert.deepEqual(env.fallbackFields, ['first-cell']);
    assert.equal(env.window.monarchMultiSelectReady802, null);
    assert.equal(env.errors.length, 1);
    env.render('second');
    assert.equal(env.requests.length, 2);
    env.requests[1].complete();
    env.componentReady();
    assert.deepEqual(env.initialized, ['second-cell']);
});
