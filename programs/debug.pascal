program debug;

var i: integer;

function test(): integer;
begin
    test := 42;
end;

begin
    i := test();
    putint(i);
end.