function processData(input) {
    //Enter your code here

//    while(totalCase != 0){
//        console.log(input[cases]);
//        cases += 1
//        totalCase -=1;
//    }
    
    
    function factorial(x) {
        if (x == 0) {
        return 1}
        else {
        return x * factorial(x - 1);
        }
}
    
} 

process.stdin.resume();
process.stdin.setEncoding("ascii");
_input = "";
process.stdin.on("data", function (input) {
    _input += input;
});

process.stdin.on("end", function () {
   processData(_input);
});

