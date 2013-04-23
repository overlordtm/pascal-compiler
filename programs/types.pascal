program types;

const
    one = 1;
    two = 2;
    three = 3;
    all = 42;
    ca = 'a';
    t = true;
    f = false;

type
    type1 = record
        a: integer;
        b: char
    end;
    type2 = record
        a: integer;
        b: char
    end;
    type3 = record
        a: integer
    end;

var
    abc: array[1..3] of char;
    globalVar: integer;
    t1: ^type1;
    t2: ^type2;
    t3: ^type3;
    tt1: type1;
    tt2: type2;
    int: integer;
    intptr: ^integer;
    bool: boolean;
    c: char;

procedure p1(arg1: integer);
    const
        one = 42;
        {p1 = -10;}
    type
        type1 = record
            a: char;
            b: integer
        end;
    var
        localVar: integer;
        localChar: char;
        t1: type1;
    begin
        localVar := one + arg1;
        t1.a := 'w';
        t1.b := one;
        
        {localVar := a;}
    end;

function f1():char;
    const
        two = 43;
    var
        localVar2: integer;
    begin
        {localVar := 666;}
        localVar2 := one + two;
    end;
    
function f2():integer;
    begin
    end;
    
function f3(one: integer):integer;
    var
        tmp: integer;
    begin
        tmp := one;
    end;

begin

    bool := -10 + 1 * 8 = 11;
    
    bool := not t or t;

    int := one + f2();

    p1(one);
    {zoki();}
    tt1.a := 13;
    tt2.a := 7;
    
    {int := tt1.a[1+1];}
    
    int := tt1.a + tt2.a;

{Strukturna enakost tipov: tipa sta enaka, ce imata enako strukturo. Tip konstante nil
se prilagodi vsakemu kazalcnemu tipu.}
    t1 := t2;
    t3 := nil;
    {t2 := t3;}

{Indeks na tabelo (s tem pa tudi meje tabele) so tipa integer.}
    abc[1] := 'a';
    {abc['g'] := 'b';}
    abc[two] := 'z';

{Globalno obmocje vidnosti imen se zacne za imenom programa in se konca na koncu programa.}

{Lokalno obmocje vidnosti imen se zacne za imenom podprogama (procedure ali funkcije)
in se konca na koncu podprograma. Deklaracije imen v lokalnem obmocju vidnosti
prekrijejo vse deklaracije istih imen v vseh zunanjih obmocjih vidnosti.}

    globalVar := one;
    {localVar := one;}
    {localVar2 := 555;}

{Vsako ime je vidno v celotnem obmocju vidnosti, v katerem je definirano.}

{Deklaracije imena podprograma ni mogoce prekriti v lokalnem obmocju vidnosti imen,
ki se zacne takoj za imenom podprograma.}
{OK}

{Vsa imena konstant, tipov, spremenljivk in podprogramov so v istem podrocje deklaracij
ne glede na obmocje vidnosti.}

{Vsak tip zapisa definira svoje lastno podrocje deklaracij imen elementov zapisa.}

    globalVar := tt1.a;
    {globalVar := a;}

{Stavek, ki je zgolj opis vrednosti, mora biti klic procedure.}

    p1(one);
    c := f1();
    {p1(ca);}
    {f1();}

{V prireditvenem stavku morata biti tipa leve in desne strani enaka, oba bodisi atomarna
bodisi kazalcna tipa.}
    int := one;
    intptr := ^one;
    {int := intptr;}

{Pogoj v stavku if in v stavku while mora biti tipa boolean.}
    if one > two then int := one;
    {if one then int := one;}
    if false then int := one;
    
    while t do begin end;
    while 1 < all do begin end;
    while c > c do begin end;
    {while c do begin end;}
    {while 0+5 do begin end;}

{Spremenljivka v zanki for mora biti tipa integer, prav tako spodnja in zgornja meja.}
    for int:=1 to all do
    begin
    end;
    
    {for undefined:=1 to all do
    begin
    end;}
    
    {for int:=1 to 'c' do begin end;}

{Argumenta (ali argument) operatorjev +, -, * in div morata biti tipa integer, rezultat
je tipa integer.}
    int := one + two;
    int := one - two;
    int := 42 * two;
    int := one div two;
    int := one div 1;
    
    {intptr := one * two;}
    
{Argumenta (ali argument) operatorjev and, or in not morata biti tipa boolean, rezultat
je tipa boolean.}

    bool := t or f;
    bool := t and f;
    bool := not f;
    {bool := not 1;}
    {bool := 1 and 2;}
    {bool := 1 or 'c';}

{Argumenta (ali argument) operatorjev =, <>, <, >, <= in >= morata biti bodisi istega
atomarnega tipa bodisi istega kazalcnega tipa, rezultat je tipa boolean.}

    bool := int = one;
    bool := int <> one;
    bool := int > one;
    bool := int < one;
    bool := int <= one;
    bool := int >= one;
    {bool := intptr > int;}
    
{Argument prefiksnega operatorja ^ je poljubnega tipa, rezultat je kazalec na ta tip}

    intptr := ^one;
    intptr := ^int;
    {intptr := t1;}
    {intptr := ^bool;}

{Argument postfiksnega operatorja ^ je poljubni kazalcni tip, rezultat je tipa, na katerega
kaze kazalec.}

    int := intptr^;
    one := intptr^;    

{Levi argument operatorja . mora biti zapis, desni argument mora biti ime elementa
zapisa, rezultat je tipa elementa zapisa.}

    int := tt1.a;
    c := tt1.b;
    c := t1^.b;
    {int := tt1.b;}
    {int := tt1.nonexistent;}
    
    {int := krneki.a;}

{Levi argument operatorja [] mora biti tabela, desni argument mora biti tipa integer,
rezultat je tipa osnovnega elementa tabele.}

    c := abc[1];
    c := abc[int];
    c := abc[intptr^];
    {int := abc[3];}
    {c := abc['c'];}
    

{Tip izraza za dodeljevanje pomnilnika je integer.}
{ a to sploh mamo? }

end.
