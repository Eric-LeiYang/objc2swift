/**
 * This file is part of objc2swift.
 * https://github.com/yahoojapan/objc2swift
 *
 * Copyright (c) 2015 Yahoo Japan Corporation
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package org.objc2swift.converter

import org.objc2swift.converter.ObjCParser._

import scala.collection.JavaConversions._

trait StatementVisitor {
  this: ObjC2SwiftBaseConverter =>

  import org.objc2swift.converter.util._

  /**
   * compound_statement:
   *   '{' (declaration|statement_list)* '}' ;
   *
   * @param ctx the parse tree
   **/
  override def visitCompoundStatement(ctx: CompoundStatementContext): String =
    visitChildren(ctx)

  /**
   * statement_list:
   *   statement+ ;
   *
   * @param ctx the parse tree
   **/
  override def visitStatementList(ctx: StatementListContext): String =
    visitChildren(ctx, "\n")

  /**
   * statement
   *   : labeled_statement
   *   | expression ';'
   *   | compound_statement
   *   | selection_statement
   *   | iteration_statement
   *   | jump_statement
   *   | synchronized_statement
   *   | autorelease_statement
   *   | try_block
   *   | ';'
   *   ;
   *
   * @param ctx the parse tree
   **/
  override def visitStatement(ctx: StatementContext): String =
    visitChildren(ctx)


  /**
   * jump_statement
   *   : 'goto' identifier ';'
   *   | 'continue' ';'
   *   | 'break' ';'
   *   | 'return' expression? ';'
   *   ;
   *
   * MEMO: no support for 'goto'
   *
   * @param ctx the parse tree
   **/
  override def visitJumpStatement(ctx: JumpStatementContext): String =
    ctx.getChild(0) match {
      case TerminalText("return") =>
        List("return", visit(ctx.expression())).filter(_.nonEmpty).mkString(" ")
      case TerminalText("break") =>
        "" // TODO
      case TerminalText("continue") =>
        "continue"
      case _ =>
        ""
    }

  /**
   * selection_statement
   *   : 'if' '(' expression ')' statement ('else' statement)?
   *   | 'switch' '(' expression ')' statement
   *   ;
   *
   * @param ctx the parse tree
   **/
  override def visitSelectionStatement(ctx: SelectionStatementContext): String =
    visitChildrenAs(ctx) {
      case TerminalText("if")     => "if"
      case TerminalText("else")   => extractElseIf(ctx.statement(1))
      case TerminalText("switch") => "switch"
      case c: ExpressionContext   => visit(c)
      case c: StatementContext if !isVisited(c) => processBlock(c)
    }


  private def extractElseIf(ctx: StatementContext): String = {
    ctx.children.toList match {
      case List(c: SelectionStatementContext) if c.getChild(0).getText == "if" =>
        setVisited(ctx)
        s"else ${visit(c)}"
      case _ =>
        "else"
    }
  }

  /**
   * labeled_statement
   *   : identifier ':' statement
   *   | 'case' constant_expression ':' statement
   *   | 'default' ':' statement
   *   ;
   *
   * @param ctx the parse tree
   **/
  override def visitLabeledStatement(ctx: LabeledStatementContext): String =
    //TODO fix indent bug
    visitChildrenAs(ctx, "") {
      case TerminalText("case")    => "case "
      case TerminalText("default") => "default"
      case TerminalText(":")       => ":\n"
      case c: ConstantExpressionContext => visit(c)
      case c: StatementContext => indent(visit(c))
    }

  /**
   * for_in_statement:
   *   'for' '(' type_variable_declarator 'in' expression? ')' statement;
   *
   * @param ctx the parse tree
   **/
  override def visitForInStatement(ctx: ForInStatementContext): String =
    visitChildrenAs(ctx) {
      case TerminalText("for")              => "for"
      case TerminalText("in")               => "in"
      case c: ExpressionContext             => visit(c)
      case c: TypeVariableDeclaratorContext => visit(c)
      case c: StatementContext              => processBlock(c)
    }

  /**
   * for_statement:
   *   'for' '(' ( (declaration_specifiers init_declarator_list) | expression)? ';'
   *             expression? ';'
   *             expression? ')'
   *   statement;
   *
   * @param ctx the parse tree
   **/
  override def visitForStatement(ctx: ForStatementContext): String =
    visitChildrenAs(ctx, "") {
      case TerminalText("for")             => "for "
      case TerminalText(";")               => "; "
      case c: ExpressionContext            => visit(c)
      case d: DeclarationSpecifiersContext => processForInitializer(d, ctx.initDeclaratorList().get)
      case c: StatementContext             => s" ${processBlock(c)}"
    }

  /**
   * while_statement:
   *   'while' '(' expression ')' statement;
   *
   * @param ctx the parse tree
   **/
  override def visitWhileStatement(ctx: WhileStatementContext): String =
    visitChildrenAs(ctx) {
      case TerminalText("while") => "while"
      case c: ExpressionContext  => visit(c)
      case c: StatementContext   => processBlock(c)
    }

  /**
   * do_statement:
   *   'do' statement 'while' '(' expression ')' ';';
   *
   * @param ctx the parse tree
   **/
  override def visitDoStatement(ctx: DoStatementContext): String =
    visitChildrenAs(ctx, "") {
      case TerminalText("do")    => "repeat {\n"
      case TerminalText("while") => s"} while"
      case c: ExpressionContext  => s" ${visit(c)}\n"
      case c: StatementContext   => indent(visitChildren(c)) + "\n"
    }


  private def processBlock(ctx: StatementContext): String =
    s"""|{
        |${indent(visit(ctx))}
        |}""".stripMargin


  private def processForInitializer(d: DeclarationSpecifiersContext, i: InitDeclaratorListContext): String = {
    i.initDeclarator() match {
      case Nil =>
        ""
      case list =>
        "var " + list.flatMap(processForInitDeclarator(d, _)).mkString(", ")
    }
  }

  private def processForInitDeclarator(d: DeclarationSpecifiersContext, ctx: InitDeclaratorContext): Option[String] =
    for {
      decl <- ctx.declarator()
      dirDecl <- decl.directDeclarator()
      id <- dirDecl.identifier()
      init <- ctx.initializer()
    } yield s"${visit(id)} = ${visit(init)}" // TODO consider declarationSpecifiers
}
