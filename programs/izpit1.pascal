program izpit1;

function factrec(n: integer);
    begin
        if n < 1 then
            factrec := 1
        else
            factrec := factrec(n-1) * n;
    end;

function fact(n: integer);
    var acc: integer;
        i: integer;

    begin
        acc := 1;
        for i:=1 to n do
            acc := acc * i;
        fact := acc;
    end;


begin
    putint(fact(-1));
    putch(chr(10));
    putint(factrec(-1));
    putch(chr(10));
    
    putint(fact(1));
    putch(chr(10));
    putint(factrec(1));
    putch(chr(10));

    putint(fact(5));
    putch(chr(10));
    putint(factrec(5));
    putch(chr(10));
end.