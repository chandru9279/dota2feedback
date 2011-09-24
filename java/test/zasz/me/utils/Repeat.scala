package zasz.me.utils

class Repeat(n: Int)  {
  def times[A](f: => A) { loop(f, n) }
  private def loop[A](f: => A, n: Int) { if (n > 0) { f; loop(f, n-1) } }
}