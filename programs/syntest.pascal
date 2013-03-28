{program ImePrograma;
    const
        konstanta = 43;
        konst2 = 42;
    var
        lol: bool;
	bol: integer;
    type
        chessboard = record
                num_queens : integer;
                used_cols : array [1..chessboard_size] of integer;
                free_lins : array [1..chessboard_size] of boolean;
            end;
begin
    solver()
end.
}
program test;
    const
        SIZE = 42;
    type
        zoki = integer;
        zoki2 = record
            a : integer;
            b : boolean;
            c : array[1..SIZE] of integer
        end;
    var
        lol : bool;
        lol2 : integer;
    procedure test();
    begin
    end;
begin
end.
