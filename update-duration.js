// Two manual marker calls. Call start() at the beginning of the process
// and end() when it finishes. end() returns a formatted string like:
//   "Update duration: 2:05.123"

let _startTime = null;

function start() {
    _startTime = Date.now();
}

function end() {
    if (_startTime === null) {
        throw new Error("start() must be called before end().");
    }

    const elapsedMs = Date.now() - _startTime;
    _startTime = null;

    const totalMs = Math.round(elapsedMs);
    const minutes = Math.floor(totalMs / 60000);
    const seconds = Math.floor((totalMs % 60000) / 1000);
    const milliseconds = totalMs % 1000;

    const ss = String(seconds).padStart(2, "0");
    const mmm = String(milliseconds).padStart(3, "0");

    return `Update duration: ${minutes}:${ss}.${mmm}`;
}

// Example:
// start();
// // ... your process ...
// const result = end();
// console.log(result); // -> "Update duration: 2:05.123"
