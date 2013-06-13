program izpit3;

const size = 3;

var arr: array [2..size] of integer;
    i: integer;

begin

    for i:=2 to 3 do
        arr[i] := 65;

    for i:=2 to 3 do
        putch(chr(arr[i]));
end.