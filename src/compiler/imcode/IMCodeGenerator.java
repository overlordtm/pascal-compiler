package compiler.imcode;

import java.util.LinkedList;

import compiler.abstree.AbsVisitor;
import compiler.abstree.tree.*;
import compiler.frames.*;
import compiler.semanal.*;
import compiler.semanal.type.*;

public class IMCodeGenerator implements AbsVisitor {

	public static LinkedList<ImcChunk> chunks = new LinkedList<ImcChunk>();
	ImcCode code;
	private boolean inMem = true;

	@Override
	public void visit(AbsAlloc acceptor) {
		ImcCALL malloc = new ImcCALL(FrmLabel.newLabel("malloc"));
		int argSize = SemDesc.getActualType(acceptor.type).size();
		malloc.args.add(new ImcCONST(SysLib.FAKE_FP));
		malloc.size.add(4);
		malloc.args.add(new ImcCONST(argSize));
		malloc.size.add(4);
		code = (ImcCode) malloc;
	}

	@Override
	public void visit(AbsArrayType acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsAssignStmt acceptor) {
		acceptor.dstExpr.accept(this);
		ImcExpr dst = (ImcExpr) code;
		acceptor.srcExpr.accept(this);
		ImcExpr src = (ImcExpr) code;

		code = new ImcMOVE(dst, src);
	}

	@Override
	public void visit(AbsAtomConst acceptor) {
		int val = 0;
		switch (acceptor.type) {
		case AbsAtomConst.BOOL:
			val = acceptor.value.equals("true") ? 1 : 0;
			break;
		case AbsAtomConst.CHAR:
			val = (int) acceptor.value.charAt(1);
			break;
		case AbsAtomConst.INT:
			val = Integer.parseInt(acceptor.value);
			break;
		}
		code = new ImcCONST(val);
	}

	@Override
	public void visit(AbsAtomType acceptor) {
	}

	@Override
	public void visit(AbsBinExpr acceptor) {
		switch (acceptor.oper) {
		case AbsBinExpr.RECACCESS:
			inMem = false;
			acceptor.fstExpr.accept(this);
			ImcExpr record = (ImcExpr) code;
			SemRecordType recordType = (SemRecordType) SemDesc
			        .getActualType(acceptor.fstExpr);
			String field = ((AbsValName) acceptor.sndExpr).name;

			SemType fieldType = null;
			int offset = 0;
			for (int i = 0; i < recordType.getNumFields(); i++) {
				if (field.equals(recordType.getFieldName(i).name)) {
					fieldType = recordType.getFieldType(i);
					break;
				}
				offset += recordType.getFieldType(i).size();
			}

			if (fieldType instanceof SemRecordType) {
				code = new ImcBINOP(ImcBINOP.ADD, record, new ImcCONST(offset));
			} else {
				code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, record,
				        new ImcCONST(offset)));
			}
			inMem = true;
			break;
		case AbsBinExpr.ARRACCESS:
			inMem = false;
			acceptor.fstExpr.accept(this);
			ImcExpr array = (ImcExpr) code;
			SemArrayType arrayType = (SemArrayType) SemDesc
			        .getActualType(acceptor.fstExpr);

			inMem = true;
			acceptor.sndExpr.accept(this);
			ImcExpr index = (ImcExpr) code;
			inMem = false;

			ImcBINOP arrIndex = new ImcBINOP(ImcBINOP.SUB, index, new ImcCONST(
			        arrayType.loBound));
			ImcBINOP arrOffset = new ImcBINOP(ImcBINOP.MUL, arrIndex,
			        new ImcCONST(arrayType.type.size()));

			code = new ImcMEM(new ImcBINOP(ImcBINOP.ADD, array, arrOffset));
			inMem = true;
			break;
		default:
			acceptor.fstExpr.accept(this);
			ImcExpr fstExpr = (ImcExpr) code;
			acceptor.sndExpr.accept(this);
			ImcExpr sndExpr = (ImcExpr) code;
			code = new ImcBINOP(acceptor.oper, fstExpr, sndExpr);
			break;
		}

	}

	@Override
	public void visit(AbsBlockStmt acceptor) {
		acceptor.stmts.accept(this);
	}

	@Override
	public void visit(AbsCallExpr acceptor) {
		FrmFrame frm = FrmDesc.getFrame(SemDesc.getNameDecl(acceptor.name));
		ImcCALL call;
		if (SysLib.hasMethod(acceptor.name.name)) {
			call = new ImcCALL(FrmLabel.newLabel(acceptor.name.name));
			call.args.add(new ImcCONST(SysLib.FAKE_FP));
			call.size.add(4);
		} else {
			call = new ImcCALL(frm.label);
			call.args.add(new ImcTEMP(frm.FP));
			call.size.add(4);
		}

		for (AbsValExpr expr : acceptor.args.exprs) {
			expr.accept(this);
			call.args.add((ImcExpr) code);
			call.size.add(4);
		}
		code = call;
	}

	@Override
	public void visit(AbsConstDecl acceptor) {
	}

	@Override
	public void visit(AbsDeclName acceptor) {
	}

	@Override
	public void visit(AbsDecls acceptor) {
		for (AbsDecl decl : acceptor.decls) {
			// pomembne samo deklaracija subrutin
			if (decl instanceof AbsFunDecl || decl instanceof AbsProcDecl) {
				decl.accept(this);
			}
		}
	}

	@Override
	public void visit(AbsExprStmt acceptor) {
		acceptor.expr.accept(this);
		code = new ImcEXP((ImcExpr) code);
	}

	@Override
	public void visit(AbsForStmt acceptor) {
		acceptor.name.accept(this);
		ImcExpr count = (code instanceof ImcEXP) ? ((ImcEXP) code).expr
		        : (ImcExpr) code;
		acceptor.loBound.accept(this);
		ImcExpr loExpr = (ImcExpr) code;
		acceptor.hiBound.accept(this);
		ImcExpr hiExpr = (ImcExpr) code;

		ImcLABEL lTrue = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL lFalse = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL lStart = new ImcLABEL(FrmLabel.newLabel());

		ImcSEQ seq = new ImcSEQ();
		seq.stmts.add(new ImcMOVE(count, loExpr));
		seq.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.GTH, loExpr, hiExpr),
		        lFalse.label, lStart.label)); // if lo > hi goto false
		seq.stmts.add(lStart);
		seq.stmts.add(new ImcCJUMP(new ImcBINOP(ImcBINOP.LEQ, count, hiExpr),
		        lTrue.label, lFalse.label)); // if not(count <= hi) goto false
		seq.stmts.add(lTrue);
		acceptor.stmt.accept(this);
		seq.stmts.add((ImcStmt) code); // do actual stuff
		seq.stmts.add(new ImcMOVE(count, new ImcBINOP(ImcBINOP.ADD, count,
		        new ImcCONST(1)))); // i++
		seq.stmts.add(new ImcJUMP(lStart.label)); // goto start
		seq.stmts.add(lFalse);

		code = seq;
	}

	@Override
	public void visit(AbsFunDecl acceptor) {
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt) code));
	}

	@Override
	public void visit(AbsIfStmt acceptor) {
		acceptor.cond.accept(this);
		ImcExpr cond = (ImcExpr) code;

		ImcLABEL lTrue = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL lFalse = new ImcLABEL(FrmLabel.newLabel());
		ImcLABEL lExit = new ImcLABEL(FrmLabel.newLabel());

		ImcSEQ seq = new ImcSEQ();
		seq.stmts.add(new ImcCJUMP(cond, lTrue.label, lFalse.label));
		seq.stmts.add(lTrue);
		acceptor.thenStmt.accept(this);
		seq.stmts.add((ImcStmt) code);
		seq.stmts.add(new ImcJUMP(lExit.label));
		seq.stmts.add(lFalse);
		acceptor.elseStmt.accept(this);
		seq.stmts.add((ImcStmt) code);
		seq.stmts.add(lExit);

		code = seq;
	}

	@Override
	public void visit(AbsNilConst acceptor) {
		code = new ImcCONST(0);
	}

	@Override
	public void visit(AbsPointerType acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsProcDecl acceptor) {
		acceptor.decls.accept(this);
		acceptor.stmt.accept(this);
		chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt) code));
	}

	@Override
	public void visit(AbsProgram acceptor) {
		// ImcDataChunk vsebuje opis globalne spremenljivke.

		for (AbsDecl decl : acceptor.decls.decls) {
			if (decl instanceof AbsVarDecl) { // global vars
				AbsVarDecl varDecl = (AbsVarDecl) decl;
				FrmVarAccess varAccess = (FrmVarAccess) FrmDesc
				        .getAccess(varDecl);
				SemType varType = SemDesc.getActualType(varDecl.type);
				chunks.add(new ImcDataChunk(varAccess.label, varType.size()));
			}
		}
		// ImcCodeChunk vsebuje celotno vmesno kodo funkcije (brez vgnezdenih
		// funkcij)
		// v obliki enega ImcMOVE ukaza, s katerim izraz funkcije priredimo
		// zacasni spremenljivki RV.
		acceptor.decls.accept(this); // deklaracija subrutin (fun in proc)

		acceptor.stmt.accept(this); // main program
		chunks.add(new ImcCodeChunk(FrmDesc.getFrame(acceptor), (ImcStmt) code));
	}

	@Override
	public void visit(AbsRecordType acceptor) {
		for (AbsDecl decl : acceptor.fields.decls) {
			decl.accept(this);
		}
	}

	@Override
	public void visit(AbsStmts acceptor) {
		ImcSEQ stmts = new ImcSEQ();
		for (AbsStmt stmt : acceptor.stmts) {
			stmt.accept(this);
			stmts.stmts.add((ImcStmt) code);
		}
		code = stmts;
	}

	@Override
	public void visit(AbsTypeDecl acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsTypeName acceptor) {
	}

	@Override
	public void visit(AbsUnExpr acceptor) {
		acceptor.expr.accept(this);
		switch (acceptor.oper) {
		case AbsUnExpr.ADD:
			break; // positive const is positive const :D
		case AbsUnExpr.SUB:
			code = new ImcBINOP(ImcBINOP.SUB, (ImcExpr) (new ImcCONST(0)),
			        (ImcExpr) code); // -42 = 0 - 42
			break;
		case AbsUnExpr.VAL:
			code = new ImcMEM((ImcExpr) code);
			break;
		case AbsUnExpr.MEM:
			code = ((ImcMEM) code).expr;
			break;
		case AbsUnExpr.NOT:
			code = new ImcBINOP(ImcBINOP.NEQ, (ImcExpr) code, (ImcExpr) code);
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
		FrmAccess access = FrmDesc.getAccess(decl);

		if (access instanceof FrmVarAccess) {
			FrmVarAccess varAccess = (FrmVarAccess) access;
			code = new ImcNAME(varAccess.label);
			if (inMem) {
				code = new ImcMEM((ImcNAME) code);
			}
		}
		if (access instanceof FrmArgAccess) {
			FrmArgAccess argAccess = (FrmArgAccess) access;
			code = new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(argAccess.frame.FP),
			        new ImcCONST(argAccess.offset));
			if (inMem) {
				code = new ImcMEM((ImcBINOP) code);
			}
		}
		if (access instanceof FrmLocAccess) {
			FrmLocAccess locAccess = (FrmLocAccess) access;
			code = new ImcBINOP(ImcBINOP.ADD, new ImcTEMP(locAccess.frame.FP),
			        new ImcCONST(locAccess.offset));
			if (inMem) {
				code = new ImcMEM((ImcBINOP) code);
			}
		}

		if (decl instanceof AbsFunDecl) {
			FrmFrame frame = FrmDesc.getFrame(decl);
			code = new ImcTEMP(frame.RV);
			if (inMem) {
				code = new ImcMEM((ImcTEMP) code);
			}

			SemType type = SemDesc.getActualType(decl);
			if (type instanceof SemRecordType || type instanceof SemArrayType) {
				code = new ImcMEM((ImcExpr) code);
			}
		}
		if (decl instanceof AbsConstDecl) {
			code = new ImcCONST(SemDesc.getActualConst(decl));
		}
	}

	@Override
	public void visit(AbsVarDecl acceptor) {
		acceptor.type.accept(this);
	}

	@Override
	public void visit(AbsWhileStmt acceptor) {
		acceptor.cond.accept(this);
		acceptor.stmt.accept(this);
	}

}
