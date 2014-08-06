# Introduction to dareshi

TODO: write [great documentation](http://jacobian.org/writing/great-documentation/what-to-write/)

Based on Stuart Sierra's components and juxt.modular.

Differences from Workflow Reloaded:

1) No side-effects in ctor. Just accept initial state.
2) Start/Stop Lifecycle: take a Component as a parameter, return a Component.
   May have side-effects.
   Always have a return value.
3) Component ctor does not get its dependencies as arguments
4) Purpose of system:
** associate names with components
** manage lifecycle of the components it contains
** Provide dependencies

Types of Components:
* State Wrapper (DB connection)
* Service Provider (email)
* Domain Model (customers) -- some aggregate operations.
** All about behavior.
** Encapsulates related dependencies (to notify customers about stuff, you probably need
access to email and the database)
** Invokes the public APIs of the components in encapsulates
** component/using takes a component instance and the name(s) of its dependencies

System/start: starts all the components it contains
assoc's dependency components (which have already been start'd) into the ones that
depend on them, before calling the dependent's (start)

Allows associative injection
(defn test-system []
  (assoc (system ...)
    :email (stub-email)
    :db (test-db)))
Assoc alternate components into system map
--->  Before calling start!!  <-----

Possible alternatives:
1) with-redefs
2) binding
Problems:
1) Delimited in time
2) Potential race conditions
3) Tightly coupled to implementation
4) Wrong level of granularity
Usually don't want to swap out individual vars.
Mostly, want to swap out, e.g. the entire database.

Web Handlers:

Do *not* def a bunch of static vars that are loaded and defined at
compile time.

(defroutes routes ...)

;; associates a component into the request
(defn wrap-app-component [f web-app]
  (fn [req]
    (f (assoc req ::web-app webapp))))

;; closure over the web-app
(defn make-handler [web-app]
  (-> routes
      (wrap-app-component web-app)
      ...))

(defrecord WebServer [web-app jetty]
  component/Lifecycle
  (start [this]
    (assoc this :jetty
      (run-jetty (make-handler web-app))))
  ...)

(defn web-server []
  (component/using (map->WebServer {})
    [:web-app]))

Could have a different component for every single route
(might make sense for an API type service)

## Tricks & Extensions

* Custom lifecycle functions
* Rename dependencies
* Add mutable references for runtime state changes
* Compose systems (they're just Components)
Probably not a good idea, but you never know

update-system: takes any arbitrary function,
calls the components in dependency order, assoc'ing
in the dependencies as it goes
update-system-reverse: vice versa

Systems are records, records are maps
* Can use merge to get a union of systems
Use namespace-qualified keys for components that are different

## Advantages

* Explicit dependencies and clear boundaries
** Isolation, decoupling
** Easier to test, refactor
* Automatic ordering of stop/start
* Easy to swap in alternate implementations
* Everything at most one map lookup away

## Disadvantages
* Requires whole-app buy-in
* Lot of work to refactor an existing app to use this model
* Lots of small maps and destructuring
* System map is too big to inspect visually
* Cannot start/stop just part of a system
* There is some boilerplate




