package com.teamsolo.bricks.annotators

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.parentOfType
import java.util.*

class FieldInjectAnnotator : Annotator {

    companion object {

        const val FIELD_INJECT_ANNOTATION_CLASS = "com.huizhixin.sys.aspect.annotation.FieldInjectHolder"

        const val SERVICE_FIELD = "service"

        const val ID_FIELD_FIELD = "idField"

        const val CONTENT_FIELD_FIELD = "contentField"

        const val MATCH_FIELD_FIELD = "matchField"

        const val METHOD_FIELD = "method"

        const val ACCORDING_FIELD_FIELD = "accordingField"

        const val METHOD_FIELD_SEPARATOR = "||"
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiAnnotation) {
            val nameReferenceElement = element.nameReferenceElement

            if (nameReferenceElement?.qualifiedName == FIELD_INJECT_ANNOTATION_CLASS) {
                val parentOfClass = element.parentOfType<PsiClass>()
                val parameterList = element.parameterList
                val attributes = parameterList.attributes

                val serviceAttr = element.findAttributeValue(SERVICE_FIELD)

                var serviceClass: PsiClass? = null

                var destClass: PsiClass? = null

                if (serviceAttr is PsiClassObjectAccessExpression) {
                    serviceClass = PsiUtil.resolveClassInType(serviceAttr.operand.type)

                    val referencedTypes = serviceClass?.extendsList?.referencedTypes

                    referencedTypes?.forEach {
                        val resolveGenerics = it.resolveGenerics()

                        resolveGenerics.element?.typeParameters?.first { param -> "T" == param.name }?.let { typeParameter ->
                            destClass = PsiUtil.resolveClassInType(resolveGenerics.substitutor.substitute(typeParameter))
                        }
                    }
                }

                for (attribute in attributes) {
                    when (attribute.name) {
                        ID_FIELD_FIELD -> {
                            val fieldName = attribute.literalValue

                            attribute.value?.let {
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

                        CONTENT_FIELD_FIELD, MATCH_FIELD_FIELD -> {
                            val fieldName = attribute.literalValue

                            attribute.value?.let {
                                val field = destClass?.findFieldByName(fieldName, true)

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

                        METHOD_FIELD -> {
                            val methodConfig = attribute.literalValue

                            val methodNames = methodConfig?.split(METHOD_FIELD_SEPARATOR)

                            attribute.value?.let {
                                val textRange = it.textRange

                                var textRangeBegin = textRange.startOffset + 1

                                methodNames?.forEach { methodName ->
                                    val method = serviceClass?.findMethodsByName(methodName, true)

                                    val exists = method?.isNotEmpty()

                                    val methodDefRange = TextRange.from(textRangeBegin, methodName.length)

                                    textRangeBegin += methodName.length + METHOD_FIELD_SEPARATOR.length

                                    if (exists == true) {
                                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(methodDefRange).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
                                    } else {
                                        holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved field").range(methodDefRange).highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL).create()
                                    }
                                }
                            }
                        }

                        ACCORDING_FIELD_FIELD -> {
                            val fieldName = attribute.literalValue

                            attribute.value?.let {
                                val field = parentOfClass?.findFieldByName(fieldName, true)

                                val exists = field != null

                                val textRange = it.textRange

                                val fieldDefRange = TextRange(textRange.startOffset + 1, textRange.endOffset - 1)

                                if (exists) {
                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(fieldDefRange).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
                                } else {
                                    holder.newAnnotation(HighlightSeverity.ERROR, "Unresolved method").range(fieldDefRange).highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL).create()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
