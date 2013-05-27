package compiler.semanal;

import compiler.abstree.AbsVisitor;
import compiler.abstree.tree.*;

public class SemNameResolver implements AbsVisitor {

	public boolean error = false;
	private static String name = SemNameResolver.class.getSimpleName();

	public void error(String s, AbsTree abs) {
		System.err.println(String.format("%s: %s at line: %d:%d in %s", name,
		        s, abs.begLine, abs.begColumn, abs.getClass().getSimpleName()));
		error = true;
	}

	@Override
	public void visit(AbsAlloc acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsArrayType acceptor) {
		acceptor.loBound.accept(this);
		acceptor.hiBound.accept(this);
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
		acceptor.dstExpr.accept(this);
		acceptor.srcExpr.accept(this);
	}

	@Override
	public void visit(AbsAtomConst acceptor) {
		switch (acceptor.type) {
		case AbsAtomConst.BOOL:
			SemDesc.setActualConst(acceptor,
			        Boolean.parseBoolean(acceptor.value) ? 1 : 0);
			break;
		case AbsAtomConst.CHAR:
			SemDesc.setActualConst(acceptor,
			        Character.getNumericValue(acceptor.value.charAt(1)));
			break;
		case AbsAtomConst.INT:
			SemDesc.setActualConst(acceptor, Integer.parseInt(acceptor.value));
			break;
		}
	}

	@Override
	public void visit(AbsAtomType acceptor) {
	}

	@Override
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
		if (acceptor.oper == AbsBinExpr.RECACCESS) {
			// ne vemo se tipov, tak da lahko sam spustimo :)
			return;
		}
		acceptor.sndExpr.accept(this);

		Integer fstVal = SemDesc.getActualConst(acceptor.fstExpr);
		Integer sndVal = SemDesc.getActualConst(acceptor.sndExpr);

		if (fstVal != null && sndVal != null) {
			switch (acceptor.oper) {
			case AbsBinExpr.ADD:
				SemDesc.setActualConst(acceptor, fstVal + sndVal);
				break;
			case AbsBinExpr.SUB:
				SemDesc.setActualConst(acceptor, fstVal - sndVal);
				break;
			case AbsBinExpr.MUL:
				SemDesc.setActualConst(acceptor, fstVal * sndVal);
				break;
			case AbsBinExpr.DIV:
				if (sndVal != 0) {
					SemDesc.setActualConst(acceptor, fstVal / sndVal);
				} else {
					error("Division by zero!", acceptor);
				}
				break;
			case AbsBinExpr.EQU:
				SemDesc.setActualConst(acceptor, fstVal == sndVal ? 1 : 0);
				break;
			case AbsBinExpr.NEQ:
				SemDesc.setActualConst(acceptor, fstVal != sndVal ? 1 : 0);
				break;
			case AbsBinExpr.LTH:
				SemDesc.setActualConst(acceptor, fstVal < sndVal ? 1 : 0);
				break;
			case AbsBinExpr.GTH:
				SemDesc.setActualConst(acceptor, fstVal > sndVal ? 1 : 0);
				break;
			case AbsBinExpr.LEQ:
				SemDesc.setActualConst(acceptor, fstVal <= sndVal ? 1 : 0);
				break;
			case AbsBinExpr.GEQ:
				SemDesc.setActualConst(acceptor, fstVal >= sndVal ? 1 : 0);
				break;
			case AbsBinExpr.AND:
				SemDesc.setActualConst(acceptor, fstVal & sndVal);
				break;
			case AbsBinExpr.OR:
				SemDesc.setActualConst(acceptor, fstVal | sndVal);
				break;
			}
		}
	}

	@Override
	public void visit(AbsBlockStmt acceptor) {
		acceptor.stmts.accept(this);
	}

	@Override
	public void visit(AbsCallExpr acceptor) {
		AbsDecl method = SemTable.fnd(acceptor.name.name);

		if (method == null) {
			error(String.format("Unknown method '%s'", acceptor.name.name),
			        acceptor.name);
		} else {
			SemDesc.setNameDecl(acceptor.name, method);
		}

		// acceptor.name.accept(this); // above code should do that
		acceptor.args.accept(this);
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("Redeclared existing constant", acceptor.name);
		}
		acceptor.name.accept(this);
		acceptor.value.accept(this);

		Integer value = SemDesc.getActualConst(acceptor.value);
		if (value != null) {
			SemDesc.setActualConst(acceptor, value);
		}
	}

	@Override
	public void visit(AbsDeclName acceptor) {
	}

	@Override
	public void visit(AbsDecls acceptor) {
		for (AbsDecl d : acceptor.decls) {
			d.accept(this);
		}
	}

	@Override
	public void visit(AbsExprStmt acceptor) {
		acceptor.expr.accept(this);
	}

	@Override
	public void visit(AbsForStmt acceptor) {
		AbsDecl var = SemTable.fnd(acceptor.name.name);
		if (var == null) {
			error(String.format("Undeclared variable '%s'", acceptor.name.name),
			        acceptor.name);
		} else {
			SemDesc.setNameDecl(acceptor.name, var);
		}

		acceptor.loBound.accept(this);
		acceptor.hiBound.accept(this);
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsFunDecl acceptor) {
		SemTable.newScope();
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("No overloading allowed dude!", acceptor.name);
		}
		acceptor.pars.accept(this);
		acceptor.type.accept(this);
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		SemTable.oldScope();
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("No overloading allowed dude!", acceptor.name);
		}
	}

	@Override
	public void visit(AbsIfStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenStmt.accept(this);
		acceptor.elseStmt.accept(this);
	}

	@Override
	public void visit(AbsNilConst acceptor) {

	}

	@Override
	public void visit(AbsPointerType acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsProcDecl acceptor) {
		SemTable.newScope();
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("No overloading allowed dude!", acceptor);
		}
		acceptor.name.accept(this);
		acceptor.pars.accept(this);
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		SemTable.oldScope();
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("No overloading allowed dude!", acceptor);
		}
	}

	@Override
	public void visit(AbsProgram acceptor) {
		SysLib.bootstrap();
		acceptor.decls.accept(this);
		acceptor.name.accept(this);
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsRecordType acceptor) {
		SemTable.newScope();
		acceptor.fields.accept(this);
		SemTable.oldScope();
	}

	@Override
	public void visit(AbsStmts acceptor) {
		for (AbsStmt stmt : acceptor.stmts) {
			stmt.accept(this);
		}
	}

	@Override
	public void visit(AbsTypeDecl acceptor) {
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error(String.format("Redeclared existing type '%s'", acceptor.name.name),
			        acceptor.name);
		}
		acceptor.name.accept(this);
		acceptor.type.accept(this);
		SemDesc.setNameDecl(acceptor.name, acceptor);
	}

	@Override
	public void visit(AbsTypeName acceptor) {
		AbsDecl typedecl = SemTable.fnd(acceptor.name);
		if (typedecl == null) {
			error(String.format("Unknown type '%s'", acceptor.name), acceptor);
		} else {
			SemDesc.setNameDecl(acceptor, typedecl);
		}
	}

	@Override
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		Integer val = SemDesc.getActualConst(acceptor.expr);
		if (val != null)
			switch (acceptor.oper) {
			case AbsUnExpr.ADD:
				SemDesc.setActualConst(acceptor, val);
				break;
			case AbsUnExpr.SUB:
				SemDesc.setActualConst(acceptor, -val);
				break;
			}
	}

	@Override
	public void visit(AbsValExprs acceptor) {
		for (AbsValExpr expr : acceptor.exprs) {
			expr.accept(this);
		}
	}

	@Override
	public void visit(AbsValName acceptor) {
		AbsDecl decl = SemTable.fnd(acceptor.name);
		if (decl == null) {
			error(String.format("Undefined variable '%s'", acceptor.name), acceptor);
		} else {
			SemDesc.setNameDecl(acceptor, decl);
			Integer val = SemDesc.getActualConst(decl);
			if (val != null) {
				SemDesc.setActualConst(acceptor, val);
			}
		}
	}

	@Override
	public void visit(AbsVarDecl acceptor) {
		try {
			SemTable.ins(acceptor.name.name, acceptor);
		} catch (SemIllegalInsertException e) {
			error("Redeclared existing variable", acceptor.name);
		}
		acceptor.name.accept(this);
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsWhileStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.stmt.accept(this);
	}

}
