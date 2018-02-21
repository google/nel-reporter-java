# Network Error Logging reporter

This library implements a reporter for the [Reporting][] and [Network Error
Logging][] (NEL) specifications.  These specs allow site owners to instruct
browsers and other user agents to collect and report on reliability information
about the site.  This gives you the same information as you'd get from your
server logs, but collected from your clients.  This client-side data set will
include information about failed requests that never made it to your serving
infrastructure.

[Reporting]: https://wicg.github.io/reporting/
[Network Error Logging]: https://wicg.github.io/network-error-logging/

This library provides a full working implementation of the specs, with one
glaring omission: we don't handle any of the actual communication of sending and
receiving HTTP requests.  This lets you plug this library into *any* HTTP
request library; we take of parsing and managing the reporting instructions for
each origin, caching reports, and deciding which collector to send each report
to.  You provide an implementation of the `ReportDeliverer` interface to handle
the actual HTTP communication using the library that you're integrating with.
