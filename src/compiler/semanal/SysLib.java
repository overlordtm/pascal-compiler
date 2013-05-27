package compiler.semanal;

import java.util.*;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SysLib {

	public static final int FAKE_FP = 424242;
	private static TreeSet<String> sysMethods = new TreeSet<String>();

	public static void bootstrap() {

		/*
		 * procedure putch(c:char);
		 * procedure putint(i:integer);
		 * function getch():char;
		 * function getint():integer;
		 * function ord(c:char):integer;
		 * function chr(i:integer):char;
		 * procedure free(p:pointer);
		 */
		createProcedure("putch", SemAtomType.CHAR);
		createProcedure("putint", SemAtomType.INT);
		createFunction(SemAtomType.CHAR, "getch");
		createFunction(SemAtomType.INT, "getint");
		createFunction(SemAtomType.INT, "ord", SemAtomType.CHAR);
		createFunction(SemAtomType.CHAR, "chr", SemAtomType.INT);
		createProcedure("free", SemAtomType.VOID);
	}

	private static void createProcedure(String name, Integer... args) {
		createFunction(SemAtomType.VOID, name, args);
	}

	private static void createFunction(int returnValue, String name,
	        Integer... args) {
		try {
			sysMethods.add(name);
			AbsDecls argDecls = new AbsDecls();
			SemSubprogramType type = new SemSubprogramType(new SemAtomType(returnValue));

			for(int i = 0; i < args.length; i++) {
				AbsVarDecl decl = new AbsVarDecl(new AbsDeclName("arg_" + i), new AbsAtomType(args[i]));
				argDecls.decls.add(decl);
				type.addParType(new SemAtomType(args[i]));
			}

			if(returnValue == SemAtomType.VOID) {
				AbsProcDecl acceptor = new AbsProcDecl(new AbsDeclName(name),
				        argDecls, new AbsDecls(), new AbsBlockStmt(new AbsStmts()));
				SemTable.ins(acceptor.name.name, acceptor);
				SemDesc.setActualType(acceptor, type);
			} else {
				AbsFunDecl acceptor = new AbsFunDecl(new AbsDeclName(name), argDecls, new AbsAtomType(returnValue), new AbsDecls(), new AbsBlockStmt(new AbsStmts()));
				SemTable.ins(acceptor.name.name, acceptor);
				SemDesc.setActualType(acceptor, type);
			}

		} catch (SemIllegalInsertException e) {
			System.out.println("Huston, we have a problem!: " + name);
			e.printStackTrace();
		}
	}

	public static boolean hasMethod(String name) {
		return sysMethods.contains(name);
	}

}