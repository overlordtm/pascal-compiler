package compiler.semanal;

import java.util.TreeSet;

import compiler.abstree.AbsVisitor;
import compiler.abstree.tree.*;
import compiler.semanal.type.*;

public class SemTypeChecker implements AbsVisitor {

	public boolean error = false;
	private static String name = SemTypeChecker.class.getSimpleName();

	private SemType typeInt = new SemAtomType(SemAtomType.INT);
	private SemType typeBool = new SemAtomType(SemAtomType.BOOL);
	private SemType typeChar = new SemAtomType(SemAtomType.CHAR);
	private SemType typeVoid = new SemAtomType(SemAtomType.VOID);

	public void error(String s, AbsTree abs) {
		System.err.println(String.format("%s: %s at line: %d:%d in %s", name, s,
		        abs.begLine, abs.begColumn, abs.getClass().getSimpleName()));
		error = true;
	}

	@Override
	public void visit(AbsAlloc acceptor) {
		acceptor.type.accept(this);
		SemType a = SemDesc.getActualType(acceptor.type);
		if (a != null) {
			SemDesc.setActualType(acceptor, new SemPointerType(a));
		}
	}

	@Override
	public void visit(AbsArrayType acceptor) {
		acceptor.loBound.accept(this);
		acceptor.hiBound.accept(this);
		acceptor.type.accept(this);
		SemType type = SemDesc.getActualType(acceptor.type);
		SemType hiType = SemDesc.getActualType(acceptor.hiBound);
		SemType loType = SemDesc.getActualType(acceptor.loBound);
		
		Integer loBound = SemDesc.getActualConst(acceptor.loBound);
		Integer hiBound = SemDesc.getActualConst(acceptor.hiBound);
		
		if (type != null) {
			if(hiType.coercesTo(typeInt) && loType.coercesTo(typeInt)) {
				SemDesc.setActualType(acceptor, new SemArrayType(type, loBound,
				        hiBound));
			} else {
				error("Array bounds should be integers", acceptor);
			}
		
		} else {
			error("No type found", acceptor);
		}
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
		acceptor.dstExpr.accept(this);
		acceptor.srcExpr.accept(this);
		SemType ltype = SemDesc.getActualType(acceptor.dstExpr);
		SemType rtype = SemDesc.getActualType(acceptor.srcExpr);

		if (ltype != null && rtype != null) {
			if (ltype.coercesTo(rtype)) {
				if (ltype instanceof SemAtomType
				        || ltype instanceof SemPointerType) {
					SemDesc.setActualType(acceptor, ltype);
				} else {
					error("Pointer type error", acceptor);
				}
			} else {
				error("Type missmatch", acceptor);
			}
		} else {
			error("No type found", acceptor);
		}
	}

	@Override
	public void visit(AbsAtomConst acceptor) {
		SemAtomType type = new SemAtomType(acceptor.type);
		SemDesc.setActualType(acceptor, type);
	}

	@Override
	public void visit(AbsAtomType acceptor) {
		SemAtomType type = new SemAtomType(acceptor.type);
		SemDesc.setActualType(acceptor, type);
	}

	@Override
	public void visit(AbsBinExpr acceptor) {
		acceptor.fstExpr.accept(this);
		acceptor.sndExpr.accept(this);
		SemType ftype = SemDesc.getActualType(acceptor.fstExpr);
		SemType stype = SemDesc.getActualType(acceptor.sndExpr);

		if (ftype != null
		        && (stype != null || acceptor.oper == AbsBinExpr.RECACCESS)) {
			switch (acceptor.oper) {
			case AbsBinExpr.ADD:
			case AbsBinExpr.SUB:
			case AbsBinExpr.MUL:
			case AbsBinExpr.DIV:
				if (ftype.coercesTo(stype)) {
					if (ftype.coercesTo(typeInt)) {
						SemDesc.setActualType(acceptor, ftype);
					} else {
						error("Do math with integers ;)", acceptor);
					}
				} else {
					error("Type missmatch", acceptor);
				}
				break;
			case AbsBinExpr.EQU:
			case AbsBinExpr.NEQ:
			case AbsBinExpr.LTH:
			case AbsBinExpr.GTH:
			case AbsBinExpr.LEQ:
			case AbsBinExpr.GEQ:
				if (ftype.coercesTo(stype)) {
					if (ftype instanceof SemAtomType
					        || ftype instanceof SemPointerType) {
						SemDesc.setActualType(acceptor, typeBool);
					} else {
						error("incompatible variables for comparison ;)", acceptor);
					}
				} else {
					error("Type missmatch", acceptor);
				}
				break;
			case AbsBinExpr.AND:
			case AbsBinExpr.OR:
				if (ftype.coercesTo(stype)) {
					if (ftype.coercesTo(typeBool)) {
						SemDesc.setActualType(acceptor, ftype);
					} else {
						error("Boolean expected", acceptor);
					}
				} else {
					error("Type missmatch", acceptor);
				}
				break;
			case AbsBinExpr.ARRACCESS:
				if (ftype instanceof SemArrayType) {
					if (stype.coercesTo(typeInt)) {
						SemDesc.setActualType(acceptor,
						        ((SemArrayType) ftype).type);
					} else {
						error("Integer expected", acceptor.sndExpr);
					}
				} else {
					error("Array type expected", acceptor.fstExpr);
				}
				break;
			case AbsBinExpr.RECACCESS:
				if (ftype instanceof SemRecordType) {
					SemRecordType record = (SemRecordType) ftype;
					if (acceptor.sndExpr instanceof AbsValName) {
						AbsValName valname = (AbsValName) acceptor.sndExpr;
						AbsDeclName fn;
						for(int i = 0; i < record.getNumFields(); i++) {
							fn = record.getFieldName(i);
							if(fn.name.equals(valname.name)) {
								SemDesc.setActualType(acceptor, record.getFieldType(i));
								break;
							}
						}
					} else {
						System.err.println("Wtf is going on here?!");
					}
				} else {
					error("Record expected", acceptor);
				}
				break;
			}
		} else {
			error("Unknown type", acceptor);
		}
	}

	@Override
	public void visit(AbsBlockStmt acceptor) {
		acceptor.stmts.accept(this);
	}

	@Override
	public void visit(AbsCallExpr acceptor) {
		acceptor.args.accept(this);
		acceptor.name.accept(this);

		AbsDecl decl = SemDesc.getNameDecl(acceptor.name);
		SemType type = SemDesc.getActualType(decl);

		if (type instanceof SemSubprogramType) {
			SemSubprogramType retType = new SemSubprogramType(
			        ((SemSubprogramType) type).getResultType());
			for (AbsValExpr args : acceptor.args.exprs) {
				SemType argType = SemDesc.getActualType(args);
				if (argType != null) {
					retType.addParType(argType);
				} else {
					error("Unknown type argument", args);
				}
			}
			if (type.coercesTo(retType)) {
				SemSubprogramType sub = (SemSubprogramType) type;
				SemDesc.setActualType(acceptor, sub.getResultType());
			} else {
				error("Arguments does not match declaration", acceptor);
			}
		} else {
			error("Subprogram expected", acceptor);
		}
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
		acceptor.value.accept(this);
		SemType type = SemDesc.getActualType(acceptor.value);
		if (type != null) {
			SemDesc.setActualType(acceptor, type);
		}
	}

	@Override
	public void visit(AbsDeclName acceptor) {
	}

	@Override
	public void visit(AbsDecls acceptor) {
		for (AbsDecl decl : acceptor.decls) {
			decl.accept(this);
		}
	}

	@Override
	public void visit(AbsExprStmt acceptor) {
		if (acceptor.expr instanceof AbsCallExpr) {
			AbsCallExpr call = (AbsCallExpr) acceptor.expr;
			AbsDecl proc = SemDesc.getNameDecl(call.name);
			if (proc instanceof AbsProcDecl) {
				acceptor.expr.accept(this);
				SemType type = SemDesc.getActualType(acceptor.expr);
				if (type != null) {
					SemDesc.setActualType(acceptor, type);
				} else {
					error("Unkwnon type", acceptor);
				}
			} else {
				error("Procedure expected", acceptor);
			}
		} else {
			error("Subrpogram expected", acceptor);
		}
	}

	@Override
	public void visit(AbsForStmt acceptor) {
		acceptor.name.accept(this);
		AbsDecl iter = SemDesc.getNameDecl(acceptor.name);
		SemType iterType = SemDesc.getActualType(iter);
		
		acceptor.loBound.accept(this);
		acceptor.hiBound.accept(this);

		SemType loType = SemDesc.getActualType(acceptor.loBound);
		SemType hitype = SemDesc.getActualType(acceptor.hiBound);

		if (iterType == null) {
			error("Unknown iterator type", acceptor.name);
		} else if (!iterType.coercesTo(typeInt)) {
			error("Integer expected", acceptor.name);
		}

		if (loType == null) {
			error("Unknown lo bound type", acceptor.loBound);
		} else if (!loType.coercesTo(typeInt)) {
			error("Integer expected", acceptor.loBound);
		}

		if (hitype == null) {
			error("Unknown hi bound type", acceptor.hiBound);
		} else if (!hitype.coercesTo(typeInt)) {
			error("Integer expected", acceptor.hiBound);
		}

		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsFunDecl acceptor) {
		acceptor.pars.accept(this);
		acceptor.type.accept(this);

		SemType retType = SemDesc.getActualType(acceptor.type);
		SemSubprogramType type = new SemSubprogramType(retType);

		for (AbsDecl decl : acceptor.pars.decls) {
			SemType paramType = SemDesc.getActualType(decl);
			if (paramType != null) {
				type.addParType(paramType);
			} else {
				error("Unknown function parameter type", decl);
			}
		}

		SemDesc.setActualType(acceptor, type);
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsIfStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.thenStmt.accept(this);
		acceptor.elseStmt.accept(this);
		SemType condType = SemDesc.getActualType(acceptor.cond);
		if (condType == null) {
			error("Unknown type", acceptor.cond);
		} else if (!(condType instanceof SemAtomType)
		        || ((SemAtomType) condType).type != SemAtomType.BOOL) {
			error("Boolean expected", acceptor.cond);
		}
	}

	@Override
	public void visit(AbsNilConst acceptor) {
		SemDesc.setActualType(acceptor, new SemPointerType(typeVoid));
	}

	@Override
	public void visit(AbsPointerType acceptor) {
		acceptor.type.accept(this);
		SemType type = SemDesc.getActualType(acceptor.type);
		if (type != null) {
			SemDesc.setActualType(acceptor, new SemPointerType(type));
		} else {
			error("Unknown type", acceptor);
		}
	}

	@Override
	public void visit(AbsProcDecl acceptor) {
		acceptor.pars.accept(this);
		SemSubprogramType type = new SemSubprogramType(typeVoid);

		for (AbsDecl decl : acceptor.pars.decls) {
			SemType paramType = SemDesc.getActualType(decl);
			if (paramType != null) {
				type.addParType(paramType);
			} else {
				error("Unknown procedure parameter type", decl);
			}
		}
		SemDesc.setActualType(acceptor, type);

		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsProgram acceptor) {
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
	}

	@Override
	public void visit(AbsRecordType acceptor) {
		acceptor.fields.accept(this);

		SemRecordType type = new SemRecordType();
		TreeSet<String> usedNames = new TreeSet<String>();
		for (AbsDecl d : acceptor.fields.decls) {
			if (d instanceof AbsTypeDecl) {
				AbsTypeDecl decl = (AbsTypeDecl) d;
				SemType declType = SemDesc.getActualType(decl);
				if (declType != null) {
					if (!usedNames.contains(decl.name.name)) {
						type.addField(decl.name, declType);
						usedNames.add(decl.name.name);
					} else {
						error("Duplicate fields in record", acceptor);
					}

				} else {
					error("Unknown type", acceptor);
				}
			} else {
				error("Unknown type", acceptor);
			}
		}

		SemDesc.setActualType(acceptor, type);
	}

	@Override
	public void visit(AbsStmts acceptor) {
		for (AbsStmt stmt : acceptor.stmts) {
			stmt.accept(this);
		}
	}

	@Override
	public void visit(AbsTypeDecl acceptor) {
		acceptor.type.accept(this);
		SemType type = SemDesc.getActualType(acceptor.type);
		if (type != null) {
			SemDesc.setActualType(acceptor, type);
		} else {
			error("Unknown type", acceptor);
		}
	}

	@Override
	public void visit(AbsTypeName acceptor) {
		AbsDecl decl = SemDesc.getNameDecl(acceptor);
		if (decl instanceof AbsTypeDecl) {
			SemType type = SemDesc.getActualType(decl);
			if (type != null) {
				SemDesc.setActualType(acceptor, type);
			} else {
				error("Unknown type", acceptor);
			}
		} else {
			error("Unknown type", acceptor);
		}
	}

	@Override
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		SemType type = SemDesc.getActualType(acceptor.expr);
		switch (acceptor.oper) {
		case AbsUnExpr.ADD:
		case AbsUnExpr.SUB:
			if (type instanceof SemAtomType
			        && ((SemAtomType) type).type == SemAtomType.INT) {
				SemDesc.setActualType(acceptor, type);
			} else {
				error("Integer expected", acceptor);
			}
			break;
		case AbsUnExpr.NOT:
			if (type instanceof SemAtomType
			        && ((SemAtomType) type).type == SemAtomType.BOOL) {
				SemDesc.setActualType(acceptor, type);
			} else {
				error("Boolean expected", acceptor);
			}
			break;
		case AbsUnExpr.MEM:
			if (type != null) {
				SemPointerType ptr = new SemPointerType(type);
				SemDesc.setActualType(acceptor, ptr);
			} else {
				error("Unknown type", acceptor);
			}
			break;
		case AbsUnExpr.VAL:
			if (type != null) {
				if (type instanceof SemPointerType) {
					SemDesc.setActualType(acceptor,
					        ((SemPointerType) type).type);
				} else {
					error("Pointer expected", acceptor);
				}
			} else {
				error("Unknown type", acceptor);
			}
			break;
		default:
			System.err.println("forgot to imlement something?");
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
		AbsDecl decl = SemDesc.getNameDecl(acceptor);
		SemType type = SemDesc.getActualType(decl);
		if (decl instanceof AbsFunDecl) {
			type = SemDesc.getActualType(((AbsFunDecl) decl).type);
		}
		if (type != null) {
			SemDesc.setActualType(acceptor, type);
		}
	}

	@Override
	public void visit(AbsVarDecl acceptor) {
		acceptor.type.accept(this);
		SemType type = SemDesc.getActualType(acceptor.type);
		if (type != null) {
			SemDesc.setActualType(acceptor, type);
		} else {
			error("Unknown type", acceptor);
		}
	}

	@Override
	public void visit(AbsWhileStmt acceptor) {
		acceptor.cond.accept(this);
		
		SemType cond = SemDesc.getActualType(acceptor.cond);
		if (cond == null) {
			error("Unknown type", acceptor.cond);
		} else if (!cond.coercesTo(typeBool)) {
			error("Boolean expected", acceptor.cond);
		}

		acceptor.stmt.accept(this);
	}

}
