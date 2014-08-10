# dareshi

DAtomic REalm for apache SHIro

Because why should SQL databases get to have all the fun?

## Installation

You probably don't want to. This might be slightly more substantial
than vaporware, but not much. It's basically a translation of the
first HOWTO that I found in google from java into clojure.

## Usage

Don't. You could add it to your project dependencies, and
try to use it as a back-end for your security needs, but that
would be a truly awful idea at this point.

I'm a smidge past the stage of noodling, but the single unit
test I've managed to get running so far is just validating
that I can successfully run unit tests that don't do anything.

## Notes

Currently, there isn't any concept of getting things like users,
permissions, and roles into the Realm.

AFAICT, this matches existing implementations. And, really, it
makes sense. A security library needs to minimize its attack
surface area, and user management is really a totally different
animal than actually handling the security.

At the same time, for a situation like this one...maybe that
approach doesn't really fit. Right now, I don't have a good
way to add user data so I can do any testing.

## License

Copyright © 2014 James Gatannah

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
