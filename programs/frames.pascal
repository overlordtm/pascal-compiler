program abstest;

const   a = 42;
        b = 42;

type lol = integer;

var zoki: integer;
    zoki2: boolean;
    
procedure majmun(x: integer; yy: integer);
    var xx : integer;
    procedure majmun2(y: integer);
        var zoki_majmun: integer;
        begin
            zoki_majmun := y;
        end;
    begin
        a := 42 + a;
        majmun2(a);
    end;

procedure jebise();
    begin
    end;
    
function test(): integer;
    var foo: integer;
    begin
        foo := 42;
        majmun(42, 43);
        jebise();
        test := 44;
    end;

begin
    begin
        zoki := 1 + a;
    end;
    zoki2 := true or false;
    majmun(zoki, a);
end.
