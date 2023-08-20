package com.teamsolo.bricks.annotators

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.util.parentOfType
import java.util.*

class SimpleAnnotator : Annotator {

    companion object {

        const val FIELD_INJECT_ANNOTATION_CLASS = "com.huizhixin.sys.aspect.annotation.FieldInjectHolder"

        const val ID_FIELD_FIELD = "idField"
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiAnnotation) {
            val nameReferenceElement = element.nameReferenceElement

            if (nameReferenceElement?.qualifiedName == FIELD_INJECT_ANNOTATION_CLASS) {
                val parameterList = element.parameterList

                val attributes = parameterList.attributes

                for (attribute in attributes) {
                    if (attribute.name == ID_FIELD_FIELD) {
                        val fieldName = attribute.literalValue

                        attribute.value?.let {
                            val parentOfClass = element.parentOfType<PsiClass>()

                            val field = parentOfClass?.findFieldByName(fieldName, true)

                            val exists = field != null

                            val parentOfField = element.parentOfType<PsiField>()

                            val thisFieldName = parentOfField?.name

                            val self = Objects.equals(field?.name, thisFieldName)

                            val textRange = it.textRange

                            val fieldDefRange = TextRange(textRange.startOffset + 1, textRange.endOffset - 1)

                            if (exists && !self) {
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(fieldDefRange).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
                            } else {
                                holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved field").range(fieldDefRange).highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL).create()
                            }
                        }
                    }
                }
            }
        }
    }
}
