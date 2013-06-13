program izpit3;

var arr: array [1..3] of integer;
    i: integer;

begin
    putch('a');
    putch(chr(10));
    
    for i:=1 to 10 do
        arr[i] := i+64;
    
    for i:=1 to 10 do
        putch(chr(arr[i]));
        
    putch(chr(10));
    putch('b');
    putch(chr(10));
end.