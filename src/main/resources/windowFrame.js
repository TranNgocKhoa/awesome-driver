if (!window.outerWidth || window.outerHeight) {
    const windowFrame = 85
    window.outerWidth = window.innerWidth
    window.outerHeight = window.innerHeight + windowFrame
}
