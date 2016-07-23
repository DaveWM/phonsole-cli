(ns phonsole-cli.macros)

(defmacro load-hiccup-script [path]
  "loads a javascript file into a hiccup script tag"
  [:script (slurp path)])
