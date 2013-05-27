program HelloWorld;

const   zoki = 3;
        bufferSize = 42;

var i: integer;
    cin: char;
    iin: integer;
    pi: integer;
    buffer: array[1..bufferSize] of char;
    randSeed: integer;
    tmp: integer;
    
procedure printAscii();
    const   asciiStart = 32;
            asciiEnd = 126;
            
    var i: integer;
    begin
        for i:=asciiStart to asciiEnd do
            putch(chr(i))
    end;

procedure newLine();
    begin
        putch(chr(10));
    end;
    
procedure read(b: ^array[1..bufferSize] of char; len: integer);
    var i: integer;
    begin
        for i:=1 to len do
            b^[i] := getch();
    end;
    
procedure print(b: ^array[1..bufferSize] of char; len: integer);
    var i: integer;
    begin
        for i:=1 to len do
            putch(b^[i]);
    end;

procedure srand(seed: integer);
    begin
        randSeed := seed;
    end;
    
function pow(base: integer; exp: integer):integer;
    var acc: integer;
        i: integer;
    begin
        acc := 1;
        for i:=1 to exp do
            acc := acc*base;
        pow := acc;
    end;

function test():integer;
    var lol: integer;
    begin
        test := 42;
    end;
    
procedure noop();
    begin end;

begin
    {srand(42);}
    { print ascii }
    {printAscii();}

    read(^buffer, 2);
    print(^buffer, 2);

{
    newLine();
    putch('H');
    putch('e');
    putch('l');
    putch('l');
    putch('o');
    putch(' ');
    putch('W');
    putch('o');
    putch('r');
    putch('l');
    putch('d');
    putch('!');
    putch(' ');
    newLine();
}
end.
