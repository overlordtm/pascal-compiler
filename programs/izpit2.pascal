program izpit2;

const t = true;
      f = false;

var x: integer;
    xt: boolean;
    xf: boolean;


begin

    xt := true;
    xf := f;

    if xf*xf = true then
        putch('y')
    else
        putch('n');

    putch(chr(10));
end.