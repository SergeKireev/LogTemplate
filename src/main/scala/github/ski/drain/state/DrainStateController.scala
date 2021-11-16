package github.ski.drain.state

import github.ski.drain.domain.template.Template
import github.ski.drain.token.{ComparableToken, StructuredLogToken, TemplateToken, Token}

import java.util.UUID

class DrainStateController(drainState: DrainState, config: DrainConfig) {

  def getState(): DrainState = drainState

  def createTemplateFromStructured(candidate: List[StructuredLogToken]): Template = {
    Template(UUID.randomUUID(), candidate.map(Token.toTemplate))
  }

  def insertTemplate(template: Template): Unit = {
    val treeBySize = drainState.lengthMap.getOrElseUpdate(template.tokens.size, PrefixTreeInternal())
    val comparableTokens = template.tokens.map(Token.toComparable)
    val leaf = findOrCreateLeaf(comparableTokens, treeBySize)
    leaf.templates += ((template.id, template))
  }

  def upsert(candidate: List[StructuredLogToken]): (Template, List[StructuredLogToken]) = {
    val treeBySize = drainState.lengthMap.getOrElseUpdate(candidate.size, PrefixTreeInternal())
    val leaf = findOrCreateLeaf(candidate.map(Token.toComparable), treeBySize)
    val candidateTemplate = createTemplateFromStructured(candidate)
    val scoredTemplates = leaf.templates.values.map {
      case t => (t, t.similarity(candidateTemplate))
    }

    val fitTemplates = scoredTemplates
      .toList
      .sortBy(-_._2)
      .filter(_._2>config.similarityThreshold)
      .map(_._1)

    val newTemplate = fitTemplates.headOption
      .map(_.maskBy(candidate))
      .getOrElse(candidateTemplate)

    leaf.templates += ((newTemplate.id, newTemplate))
    (newTemplate, newTemplate.mask(candidate))
  }

  private def findOrCreateLeafHelper(candidate: List[ComparableToken], prefixTree: PrefixTree, depth: Int): PrefixTreeLeaf = {
    prefixTree match {
      case _:PrefixTreeLeaf if (depth < config.maxDepth) =>
        throw new Exception("Leaf encountered before fixed depth")
      case PrefixTreeInternal(m) =>
        val currentToken = candidate(depth)
        val foundNext = m.getOrElseUpdate(currentToken.print(), {
          if (depth == config.maxDepth || depth == candidate.length-1) {
            PrefixTreeLeaf()
          } else {
            PrefixTreeInternal()
          }
        })
        foundNext match {
          case p: PrefixTreeInternal =>
            findOrCreateLeafHelper(candidate, p, depth+1)
          case p: PrefixTreeLeaf => p
        }
    }
  }

  /**
   * Finds the appropriate leaf for candidate template.
   * If the internal nodes parent to the leaf don't exist, they are created.
   * @param candidate
   * @param prefixTree
   * @param depth
   * @return
   */
  private def findOrCreateLeaf(candidate: List[ComparableToken], prefixTree: PrefixTree): PrefixTreeLeaf = {
    findOrCreateLeafHelper(candidate, prefixTree, 0)
  }
}
