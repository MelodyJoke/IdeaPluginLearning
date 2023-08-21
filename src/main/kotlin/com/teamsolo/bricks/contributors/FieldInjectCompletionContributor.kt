package com.teamsolo.bricks.contributors

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.util.PsiUtil
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import com.teamsolo.bricks.Constants.Companion.ACCORDING_FIELD_FIELD
import com.teamsolo.bricks.Constants.Companion.CONTENT_FIELD_FIELD
import com.teamsolo.bricks.Constants.Companion.FIELD_INJECT_ANNOTATION_CLASS
import com.teamsolo.bricks.Constants.Companion.ID_FIELD_FIELD
import com.teamsolo.bricks.Constants.Companion.MATCH_FIELD_FIELD
import com.teamsolo.bricks.Constants.Companion.METHOD_FIELD
import com.teamsolo.bricks.Constants.Companion.SERVICE_FIELD

class FieldInjectCompletionContributor : CompletionContributor() {

    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(JavaTokenType.STRING_LITERAL).inside(PlatformPatterns.psiElement(JavaElementType.ANNOTATION_PARAMETER_LIST)), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                val position = parameters.position

                val parentOfAnnotation = position.parentOfType<PsiAnnotation>(false)

                if (parentOfAnnotation != null) {
                    if (parentOfAnnotation.qualifiedName == FIELD_INJECT_ANNOTATION_CLASS) {
                        val parentOfClass = position.parentOfType<PsiClass>()
                        val parentOfField = position.parentOfType<PsiField>()
                        val parentOfAnnotationAttribute = position.parentOfType<PsiNameValuePair>()

                        val thisFieldName = parentOfField?.name

                        val serviceAttr = parentOfAnnotation.findAttributeValue(SERVICE_FIELD)

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

                        when (parentOfAnnotationAttribute?.name) {
                            ID_FIELD_FIELD -> {
                                parentOfClass?.allFields?.forEach {
                                    val fieldName = it.name

                                    if (fieldName != thisFieldName) {
                                        result.addElement(LookupElementBuilder.create(fieldName).withIcon(AllIcons.Nodes.Field))
                                    }
                                }
                            }

                            CONTENT_FIELD_FIELD, MATCH_FIELD_FIELD -> {
                                destClass?.allFields?.forEach {
                                    val fieldName = it.name

                                    result.addElement(LookupElementBuilder.create(fieldName).withIcon(AllIcons.Nodes.Field))
                                }
                            }

                            ACCORDING_FIELD_FIELD -> {
                                parentOfClass?.allFields?.forEach {
                                    val fieldName = it.name

                                    result.addElement(LookupElementBuilder.create(fieldName).withIcon(AllIcons.Nodes.Field))
                                }
                            }

                            METHOD_FIELD -> {
                                serviceClass?.allMethods?.forEach {
                                    val methodName = it.name

                                    result.addElement(LookupElementBuilder.create(methodName).withIcon(AllIcons.Nodes.Method))
                                }
                            }
                        }
                    }
                }
            }
        })
    }
}
