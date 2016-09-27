package ua.ds.persistent

import scala.annotation.tailrec
import scala.collection.AbstractIterator

sealed trait List[+T] {
    self =>
    def zipWith[E >: T](other: List[E])(zipper: (E, E) => E): List[E] = {
        (this, other) match {
            case (List.Cons(sizeFirst, elemFirst, tailFirst), List.Cons(sizeSecond, elemSecond, tailSecond)) => List.Cons(sizeFirst, zipper(elemFirst, elemSecond), tailFirst.zipWith(tailSecond)(zipper))
            case (_, _) => List.Nil
        }
    }

    def flatMap[A]()(map: T => List[A]): List[A] = {
        this match {
            case List.Nil => List.Nil
            case List.Cons(_, elem, tail) => map(elem).concatenate(tail.flatMap()(map))
        }
    }

    def filter()(predicate: T => Boolean): List[T] = {
        this match {
            case List.Nil => List.Nil
            case List.Cons(size, elem, tail) =>
                val last = tail.filter()(predicate)
                if (predicate(elem)) List.Cons(last.size + 1, elem, last)
                else last
        }
    }

    def map[A]()(func: T => A): List[A] = {
        this match {
            case List.Nil => List.Nil
            case List.Cons(size, elem, tail) => List.Cons(size, func(elem), tail.map()(func))
        }
    }

    def fold[E >: T](init: E)(func: (E, E) => E): E

    def dropWhile()(predicate: (T) => Boolean): List[T]

    def drop(size: Int): List[T]

    def setHead[E >: T](element: E): List[E]

    def addToHead[E >: T](element: E): List[E]

    def addToTail[E >: T](element: E): List[E]

    def isEmpty: Boolean

    def size: Int

    def contains[E >: T](element: E): Boolean

    def head: Option[T]

    def tail: List[T]

    def toIterator: Iterator[T] = new AbstractIterator[T] {
        var these = self

        override def next(): T = these match {
            case List.Nil => throw new NoSuchElementException
            case List.Cons(_, elem, tail) => these = tail; elem
        }

        override def hasNext: Boolean = these.size != 0
    }

    def concatenate[E >: T](other: List[E]): List[E] = {
        this match {
            case List.Nil => other
            case List.Cons(size, elem, tail) => List.Cons(size + other.size, elem, tail.concatenate(other))
        }
    }
}

object List {
    def apply[T](): List[T] = Nil

    def apply[T](seq: T*): List[T] = {
        @tailrec
        def iteration(iterator: Iterator[T], list: List[T]): List[T] = {
            if (!iterator.hasNext) list
            else iteration(iterator, list.addToHead(iterator.next()))
        }
        iteration(seq.reverseIterator, Nil)
    }

    case object Nil extends List[Nothing] {
        override def fold[E >: Nothing](init: E)(func: (E, E) => E): E = init

        override def dropWhile()(predicate: (Nothing) => Boolean): List[Nothing] = this

        override def drop(size: Int): List[Nothing] = this

        override def setHead[E >: Nothing](element: E): List[E] = addToHead(element)

        override def addToHead[E](element: E): List[E] = Cons(1, element, Nil)

        override def addToTail[E >: Nothing](element: E): List[E] = List.Cons(1, element, List.Nil)

        override def isEmpty: Boolean = true

        override def size: Int = 0

        override def contains[E](element: E): Boolean = false

        override def head: Option[Nothing] = None

        def tail: List[Nothing] = List.Nil
    }

    final case class Cons[+T](size: Int, elem: T, tail: List[T]) extends List[T] {
        override def fold[E >: T](init: E)(func: (E, E) => E): E = {
            tail.fold(func(init, elem))(func)
        }

        override def dropWhile()(predicate: (T) => Boolean): List[T] = {
            if (predicate(elem)) tail.dropWhile()(predicate)
            else this
        }

        override def drop(size: Int): List[T] = {
            if (size == 0) this
            else tail.drop(size - 1)
        }

        override def setHead[E >: T](element: E): List[E] = Cons(size, element, tail)

        override def addToHead[E >: T](element: E): List[E] = Cons(size + 1, element, this)

        override def addToTail[E >: T](element: E): List[E] = Cons(size + 1, this.elem, tail.addToTail(element))

        override def isEmpty: Boolean = false

        override def contains[E >: T](element: E): Boolean = {
            if (element == elem) true
            else tail.contains(element)
        }

        override def head: Option[T] = Some(elem)
    }

}
