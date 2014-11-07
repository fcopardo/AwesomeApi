package com.awesomeapi

import java.sql.{Date, Timestamp}
import com.awesomeapi.libs.Time

package object domain {
  def ts(ts: Long = Time.now): Timestamp = new Timestamp(ts * 1000)
  def date(ts: Long = Time.now): Date = new Date(ts * 1000)
  def now: Timestamp = ts()
}
