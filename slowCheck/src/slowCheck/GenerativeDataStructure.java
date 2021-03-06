package slowCheck;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Predicate;


/**
 * @author peter
 */
class GenerativeDataStructure implements DataStructure {
  private final Random random;
  private final StructureNode node;

  GenerativeDataStructure(Random random, StructureNode node) {
    this.random = random;
    this.node = node;
  }

  @Override
  public int drawInt(@NotNull IntDistribution distribution) {
    int i = distribution.generateInt(random);
    node.addChild(new IntData(i, distribution));
    return i;
  }

  @NotNull
  @Override
  public GenerativeDataStructure subStructure() {
    return new GenerativeDataStructure(random, node.subStructure());
  }

  @Override
  public <T> T generateNonShrinkable(@NotNull Generator<T> generator) {
    GenerativeDataStructure data = subStructure();
    data.node.shrinkProhibited = true;
    return generator.generateUnstructured(data);
  }

  @Override
  public <T> T generateConditional(@NotNull Generator<T> generator, @NotNull Predicate<T> condition) {
    for (int i = 0; i < 100; i++) {
      GenerativeDataStructure structure = new GenerativeDataStructure(random, node.subStructure());
      T value = generator.generateUnstructured(structure);
      if (condition.test(value)) return value;
      
      node.removeLastChild(structure.node);
    }
    throw new CannotSatisfyCondition(condition);
  }
}
