let objectToInspect = window, result = [];
while (objectToInspect !== null) {
    result = result.concat(Object.getOwnPropertyNames(objectToInspect));
    objectToInspect = Object.getPrototypeOf(objectToInspect);
}
/** @noinspection */
return result.filter(i => i.match(/.+_.+_(Array|Promise|Symbol)/ig))