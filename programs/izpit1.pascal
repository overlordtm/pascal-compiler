program izpit1;

var arr: array [1..3] of integer;
    i: integer;

function test(n: integer);
    begin
        if n < 1 then
            test := 1
        else
            test := test(n-1) * n;
    end;
    
function test2(n: integer);
    var acc: integer;
        i: integer;

    begin
        acc := 1;
        for i:=1 to n do
            acc := acc * i;
        test := acc;
    end;


begin
    putch('a');
    putch(chr(10));
    
    i := test(10);
    putint(i);
        
    putch(chr(10));
    putch('b');
    putch(chr(10));
end.