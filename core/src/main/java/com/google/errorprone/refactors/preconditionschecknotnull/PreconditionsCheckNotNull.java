/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refactors.preconditionschecknotnull;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.refactors.RefactoringMatcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;

import java.util.List;

import static com.google.errorprone.BugPattern.Category.GUAVA;
import static com.google.errorprone.BugPattern.MaturityLevel.ON_BY_DEFAULT;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.*;
import static com.sun.source.tree.Tree.Kind.STRING_LITERAL;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@BugPattern(name = "Preconditions.checkNotNull",
    summary = "Literal passed as first argument to Preconditions.checkNotNull()",
    explanation =
        "Preconditions.checkNotNull() takes two arguments. The first is the reference " +
        "that should be non-null. The second is the error message to print (usually a string " +
        "literal). Often the order of the two arguments is swapped, and the reference is " +
        "never actually checked for nullity. This check ensures that the first argument to " +
        "Preconditions.checkNotNull() is not a literal.",
    category = GUAVA, severity = ERROR, maturity = ON_BY_DEFAULT)
public class PreconditionsCheckNotNull extends RefactoringMatcher<MethodInvocationTree> {

  @SuppressWarnings({"unchecked"})
  @Override
  public boolean matches(MethodInvocationTree methodInvocationTree, VisitorState state) {
    return allOf(
        methodSelect(staticMethod("com.google.common.base.Preconditions", "checkNotNull")),
        argument(0, kindIs(STRING_LITERAL, ExpressionTree.class)))
        .matches(methodInvocationTree, state);
  }

  @Override
  public Refactor refactor(MethodInvocationTree methodInvocationTree, VisitorState state) {
    List<? extends ExpressionTree> arguments = methodInvocationTree.getArguments();
    ExpressionTree stringLiteralValue = arguments.get(0);
    SuggestedFix fix = new SuggestedFix();
    if (arguments.size() == 2) {
      fix.swap(arguments.get(0), arguments.get(1));
    } else {
      fix.delete(state.getPath().getParentPath().getLeaf());
    }
    return new Refactor(stringLiteralValue, refactorMessage, fix);
  }

  public static class Scanner extends com.google.errorprone.Scanner {
    public RefactoringMatcher<MethodInvocationTree> matcher = new PreconditionsCheckNotNull();

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, VisitorState visitorState) {
      evaluateMatch(node, visitorState, matcher);
      return super.visitMethodInvocation(node, visitorState);
    }
  }

}
