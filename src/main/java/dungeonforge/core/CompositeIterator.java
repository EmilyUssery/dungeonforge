package dungeonforge.core;

import dungeonforge.items.Item;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * WEEK 13 (US-10.3) -- ITERATOR over a COMPOSITE. Flattens a tree of items into one walk by
 * keeping a STACK of iterators: one for each level we are currently inside.
 *
 * Pre-order: a container is handed out BEFORE its contents. That is what lets
 * `take iron-bound` match the chest before it ever reaches the vial inside it.
 */
public class CompositeIterator implements Iterator<Item> {

    private final Deque<Iterator<Item>> stack = new ArrayDeque<>();
    /** The iterator that handed out the last element; remove() delegates to it. */
    private Iterator<Item> lastUsed = null;
    /** The children we pushed for that element, if it was a container. */
    private Iterator<Item> descendedInto = null;

    public CompositeIterator(Iterator<Item> top) {
        stack.push(top);
    }

    @Override
    public boolean hasNext() {
        while (!stack.isEmpty() && !stack.peek().hasNext()) {
            stack.pop();                         // this level is finished; climb back up
        }
        return !stack.isEmpty();
    }

    @Override
    public Item next() {
        if (!hasNext()) throw new NoSuchElementException();
        Iterator<Item> top = stack.peek();       // peek, not pop: it may have more after this
        Item item = top.next();
        lastUsed = top;
        descendedInto = null;
        if (item.isContainer()) {
            descendedInto = item.children();
            stack.push(descendedInto);           // its contents come next
        }
        return item;                             // yield first: pre-order
    }

    @Override
    public void remove() {
        if (lastUsed == null) throw new IllegalStateException("remove() called before next()");
        // If we stepped inside the box we are deleting, step back out first, or the walk keeps
        // handing out items from a box that is gone.
        if (descendedInto != null && stack.peek() == descendedInto) {
            stack.pop();
        }
        lastUsed.remove();                       // removes from whichever level it came from
        lastUsed = null;
        descendedInto = null;
    }
}
