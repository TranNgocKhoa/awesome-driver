const stripErrorStack = stack =>
    stack
        .split('\n')
        .filter(line => !line.includes('at Object.apply'))
        .filter(line => !line.includes('at Object.get'))
        .join('\n')

const getParameterProxyHandler = {
        get (target, key) {
            try {
                if (typeof target[key] === 'function') {
                    return target[key].bind(target)
                }
                return Reflect.get(target, key)
            } catch (err) {
                err.stack = stripErrorStack(err.stack)
                throw err
            }
        },
        apply: function (target, thisArg, args) {
            const param = (args || [])[0]
            if (param === 37445) return 'Intel Inc.'
            if (param === 37446) return 'Intel(R) Iris(TM) Plus Graphics 640'
            try {
                return Reflect.apply(target, thisArg, args)
            } catch (err) {
                err.stack = stripErrorStack(err.stack)
                throw err
            }
        }
    }

;['WebGLRenderingContext', 'WebGL2RenderingContext'].forEach(function (ctx) {
    Object.defineProperty(window[ctx].prototype, 'getParameter', {
        configurable: true,
        enumerable: false,
        writable: false,
        value: new Proxy(window[ctx].prototype.getParameter, getParameterProxyHandler)
    })
})