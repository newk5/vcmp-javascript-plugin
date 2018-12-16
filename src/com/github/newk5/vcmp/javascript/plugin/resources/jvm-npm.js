/**
 *  Copyright 2014-2016 Red Hat, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
// Since we intend to use the Function constructor.
/* jshint evil: true */

module = (typeof module === 'undefined') ? {} : module;

(function () {


    NativeRequire = (typeof NativeRequire === 'undefined') ? {} : NativeRequire;
    if (typeof require === 'function' && !NativeRequire.require) {
        NativeRequire.require = require;
    }

    function Module(id, parent, core) {
        this.id = id;
        this.core = core;
        this.parent = parent;
        this.children = [];
        this.filename = id;
        this.loaded = false;

        Object.defineProperty(this, 'exports', {
            get: function () {
                return this._exports;
            }.bind(this),
            set: function (val) {
                Require.cache[this.filename] = val;
                this._exports = val;
            }.bind(this)
        });
        this.exports = {};

        if (parent && parent.children)
            parent.children.push(this);

        this.require = function (id) {
            return Require(id, this);
        }.bind(this);
    }

    Module._load = function _load(file, parent, core, main) {

        var module = new Module(file, parent, core);
        var body = readFile(module.filename, module.core);
        var dir = new File(module.filename).getParent();
        var func = new Function('exports', 'module', 'require', '__filename', '__dirname', body);
        func.apply(module, [module.exports, module, module.require, module.filename, dir]);
        module.loaded = true;
        module.main = main;
        return module.exports;
    };

    Module.runMain = function runMain(main) {

        var file = Require.resolve(main);
        Module._load(file, undefined, false, true);

    };

    function Require(id, parent) {
        var core;
        var native_;

        var file = Require.resolve(id, parent);

        if (!file) {
            if (typeof NativeRequire.require === 'function') {
                if (Require.debug) {
                    console.log(['Cannot resolve', id, 'defaulting to native'].join(' '));
                }
                native_ = NativeRequire.require(id);
                if (native_)
                    return native_;
            }
            console.log('Cannot find module ' + id + ' - MODULE_NOT_FOUND');
        }

        if (file.core) {
            file = file.path;
            core = true;
        }
        try {
            if (Require.cache[file]) {
                return Require.cache[file];
            } else if (file.endsWith('.js')) {
                return Module._load(file, parent, core);
            } else if (file.endsWith('.json')) {
                return loadJSON(file);
            }
        } catch (ex) {

            console.log('Cannot load module ' + id + ' LOAD_ERROR ' + ex);


        }
    }

    Require.resolve = function (id, parent) {
        id=id.toLowerCase();
        var roots = findRoots(parent);
        for (var i = 0; i < roots.length; ++i) {
            var root = roots[i];
            var result = resolveCoreModule(id, root) || resolveAsFile(id, root, '.js') || resolveAsFile(id, root, '.json') || resolveAsDirectory(id, root) || resolveAsNodeModule(id, root);
            if (result) {
                return result;
            }
        }
        return false;
    };
    Require.root = NPMUtils.userDir();

    Require.NODE_PATH = undefined;

    function findRoots(parent) {
        var r = [];
        r.push(findRoot(parent));
        return r.concat(Require.paths());
    }

    function parsePaths(paths) {
        if (!paths) {
            return [];
        }
        if (paths === '') {
            return [];
        }
        var osName = NPMUtils.getProp('os.name').toLowerCase();
        var separator;

        if (osName.indexOf('win') >= 0) {
            separator = ';';
        } else {
            separator = ':';
        }

        return paths.split(separator);
    }

    Require.paths = function () {
        var r = [];

        r.push(NPMUtils.getProp('user.home') + '/.node_modules');
        r.push(NPMUtils.getProp('user.home') + '/.node_libraries');

        if (Require.NODE_PATH) {
            r = r.concat(parsePaths(Require.NODE_PATH));
        } else {
            var NODE_PATH = NPMUtils.getSystemEnv().NODE_PATH;
            if (NODE_PATH) {
                r = r.concat(parsePaths(NODE_PATH));
            }
        }
        // r.push( $PREFIX + "/node/library" )
        return r;
    };

    function findRoot(parent) {
        if (!parent || !parent.id) {
            return Require.root;
        }
        var pathParts = parent.id.split(/[\/|\\,]+/g);
        pathParts.pop();
        return pathParts.join('/');
    }

    Require.debug = true;
    Require.cache = {};
    Require.extensions = {};
    require = Require;

    module.exports = Module;

    function loadJSON(file) {
        var json = JSON.parse(readFile(file));
        Require.cache[file] = json;
        return json;
    }

    function resolveAsNodeModule(id, root) {
        var base = [root, 'node_modules'].join('/');
        return resolveAsFile(id, base) || resolveAsDirectory(id, base) || (root ? resolveAsNodeModule(id, new File(root).getParent()) : false);
    }

    function resolveAsDirectory(id, root) {
        var base = [root, id].join('/');
        var file = new File([base, 'package.json'].join('/'));
        if (file.exists()) {
            try {
                var body = readFile(file.getCanonicalPath());
                var package_ = JSON.parse(body);
                if (package_.main) {
                    return (resolveAsFile(package_.main, base) || resolveAsDirectory(package_.main, base));
                }
                // if no package.main exists, look for index.js
                return resolveAsFile('index.js', base);
            } catch (ex) {
                console.log("PARSE_ERROR - Cannot load JSON file: " + ex);

            }
        }
        return resolveAsFile('index.js', base);
    }

    function resolveAsFile(id, root, ext) {
        var file;
        if (id.length > 0 && id[0] === '/') {

            file = new File(normalizeName(id, ext));
            if (!file.exists()) {
                console.log("FAILED TO FIND: " + normalizeName(id, ext));

                return resolveAsDirectory(id);
            }
        } else {

            file = new File([root, normalizeName(id, ext)].join('/'));
        }
        if (file.exists()) {
            return file.getCanonicalPath();
        }
    }

    function resolveCoreModule(id, root) {
        var name = normalizeName(id);
        var classloader = Thread.currentThread().getContextClassLoader();
        if (classloader.getResource(name)) {
            return {path: name, core: true};
        }
    }

    function normalizeName(fileName, ext) {
        var extension = ext || '.js';
        if (fileName.endsWith(extension)) {
            return fileName.toLowerCase();
        }
        return (fileName + extension).toLowerCase();
    }

    function readFile(filename, core) {
        var input;
        try {
            if (core) {
                var classloader = Thread.currentThread().getContextClassLoader();
                input = classloader.getResourceAsStream(filename);
            } else {
                input = new File(filename);
            }
            // TODO: I think this is not very efficient
            return new Scanner(input).useDelimiter('\\A').next();
        } catch (e) {
            console.error('Cannot read file [' + input + ']: ', 'IO_ERROR : ' + e);
        }
    }

    function ModuleError(message, code, cause) {
        this.code = code || 'UNDEFINED';
        this.message = message || 'Error loading module';
        this.cause = cause;
    }

    // Helper function until ECMAScript 6 is complete
    if (typeof String.prototype.endsWith !== 'function') {
        String.prototype.endsWith = function (suffix) {
            if (!suffix)
                return false;
            return this.indexOf(suffix, this.length - suffix.length) !== -1;
        };
    }

    if (typeof Boolean.prototype.endsWith !== 'function') {
        Boolean.prototype.endsWith = function (suffix) {

            return false;
        };
    }

    ModuleError.prototype = new Error();
    ModuleError.prototype.constructor = ModuleError;
}());

