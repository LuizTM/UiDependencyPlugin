
const treeJson = `%json_here%`

json = JSON.parse(treeJson)

process(json)
$("select").val('').change();
function process(treeData) {
    // Calculate total nodes, max label length
    let totalNodes = 0;
    let maxLabelLength = 0;
    // variables for drag/drop
    let selectedNode = null;
    let draggingNode = null;
    // panning variables
    let panSpeed = 200;
    let panBoundary = 20; // Within 20px from edges will pan when dragging.
    // Misc. variables
    let i = 0;
    let duration = 750;
    let root = d3.hierarchy(treeData, function (d) { return d.children; });
    let cacheRoot = root;
    // size of the diagram
//     let viewerWidth = 100;
//     let viewerHeight = 100;
    let viewerWidth = window.innerWidth;
    let viewerHeight = window.innerHeight;

    let dragStarted = null;
    let panTimer;
    let nodes;
    let tree;
    let source;
    let node;
    let t, x, y; // for centering node

    let treemap;
    //let tree = d3.tree().size([viewerHeight, viewerWidth]);


    // search feature

    function select2DataCollectName(d) {
        if (d.children)
            d.children.forEach(select2DataCollectName);
        else if (d._children)
            d._children.forEach(select2DataCollectName);
        if (!d.children) select2Data.push(d.data);
    }

    //===============================================
    function searchTree(d) {
        if (d.children)
            d.children.forEach(searchTree);
        else if (d._children)
            d._children.forEach(searchTree);
        var searchFieldValue = eval(searchField);
        if (searchFieldValue && searchFieldValue.toLowerCase().indexOf(searchText.toLowerCase()) !== -1) {
            // Walk parent chain
            var ancestors = [];
            var parent = d;
            while (typeof (parent) !== "undefined" && parent !== null) {
                ancestors.push(parent);
                //console.log(parent);
                parent.class = "found";
                parent = parent.parent;
            }
            // console.log(ancestors);
        }
    }

    //===============================================
    function collapseAllNotFound(d) {
        if (d.children) {

            if (d.class !== "found") {
                d._children = d.children;
                d._children.forEach(collapseAllNotFound);
                d.children = null;
            } else
                d.children.forEach(collapseAllNotFound);
        }
    }
    //===============================================

    //===============================================

    function clearAll(d) {
        d.class = "";
        if (d.children)
            d.children.forEach(clearAll);
        else if (d._children)
            d._children.forEach(clearAll);
    }
    //===============================================

    function expandAll(d) {
        if (d._children) {
            d.children = d._children;
            d.children.forEach(expandAll);
            d._children = null;
        } else if (d.children)
            d.children.forEach(expandAll);
    }

    //===============================================
    $("#btnReset").click(function() {
        // root._children.forEach(function (child) {
        //     collapse(child);
        // });
        d3.select("g").selectAll("*").remove();
        root.children.forEach(function reset(d){
            if (d.children) {
                d.class = null
                d._children = d.children;
                d._children.forEach(reset)
                d.children = null;
              }
        });
        update(root);
        centerNode(root)
        $("select").val('').change();
    });
    $("#search").on("select2:select", function (e) {
        clearAll(root);
        expandAll(root);
        update(root);
        searchField = "d.data.name";
        searchText = e.params.data.text;
        searchTree(root);
        root.children.forEach(collapseAllNotFound);
        update(root);
        centerNode(root)
    })

    function findParent(datum) {
        if (datum.depth < 2) {
            return datum.data.name
        } else {
            return findParent(datum.parent)
        }
    }

    function findParentLinks(datum) {
        if (datum.depth < 2) {
            return datum.data.name
        } else {
            return findParent(datum.parent)
        }
    }

    // search feature




    // A recursive helper function for performing some setup by walking through all nodes
    function visit(parent, visitFn, childrenFn) {
        if (!parent) return;
        visitFn(parent);
        let children = childrenFn(parent);
        if (children) {
            let count = children.length;
            for (let i = 0; i < count; i++) {
                visit(children[i], visitFn, childrenFn);
            }
        }
    }

    // Call visit function to establish maxLabelLength
    visit(treeData, function (d) {
        totalNodes++;
        maxLabelLength = Math.max(d.name.length, maxLabelLength);
    },
        function (d) { return d.children && d.children.length > 0 ? d.children : null; });


    // Define the zoom function for the zoomable tree
    function zoom() {
        // origigi transform
        if (d3.event.transform !== null) {
            svgGroup.attr("transform", d3.event.transform);
        }
    }
    // Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.

    function centerNode(source) {
        t = d3.zoomTransform(baseSvg.node());
        x = -source.y0;
        y = -source.x0;
        x = x * t.k + viewerWidth / 4;
        y = y * t.k + viewerHeight / 4;
        d3.select('svg').transition().duration(duration)
                                     .call( zoomListener.transform, d3.zoomIdentity.translate(x,y).scale(t.k) );
  }
      // define the zoomListener which calls the zoom function on the "zoom" event constrained within the scaleExtents
      var zoomListener = d3.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);


    // define the baseSvg, attaching a class for styling and the zoomListener
    let baseSvg = d3.select("#tree-container").append("svg")
//        .attr("width", viewerWidth)
//        .attr("height", viewerHeight)
        .style("background-color", "white")
        .attr("class", "overlay")
        .call(zoomListener);


    // Helper functions for collapsing and expanding nodes.
    function collapse(d) {
        if (d.children) {
            d._children = d.children;
            d._children.forEach(collapse)
            d.children = null;
            }
    }

    function expand(d) {
        if (d._children) {
            d.children = d._children;
            d.children.forEach(expand);
            d._children = null;
        }
    }

    let overCircle = function (d) {
        selectedNode = d;
        updateTempConnector();
    };
    let outCircle = function (d) {
        selectedNode = null;
        updateTempConnector();
    };

    // Function to update the temporary connector indicating dragging affiliation
    let updateTempConnector = function () {
        let data = [];
        if (draggingNode !== null && selectedNode !== null) {
            // have to flip the source coordinates since we did this for the existing connectors on the original tree
            data = [{
                source: {
                    x: selectedNode.y0,
                    y: selectedNode.x0
                },
                target: {
                    x: draggingNode.y0,
                    y: draggingNode.x0
                }
            }];
        }
        let link = svgGroup.selectAll(".templink").data(data);

        link.enter().append("path")
            .attr("class", "templink")
            .attr("d", function (d) {
                let o = { x: source.x0, y: source.y0 };
                return diagonal(o, o);
            })
            .attr('pointer-events', 'none');

        link.attr("d", function (d) {
            let o = { x: source.x0, y: source.y0 };
            return diagonal(o, o);
        });

        link.exit().remove();
    };

    // Toggle children function

    function toggleChildren(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else if (d._children) {
            d.children = d._children;
            d._children = null;
        }
        return d;
    }

    // Toggle children on click.

    function click(d) {
        // if (d3.event.defaultPrevented) return; // click suppressed
        d = toggleChildren(d);
        update(d);
        centerNode(d);
    }

    function diagonal(s, d) {
        if (s !== null && d !== null) {
            let path = "M " + s.y + " " + s.x
                + " C " + ((s.y + d.y) / 2) + " " + s.x + ","
                + ((s.y + d.y) / 2) + " " + d.x + ","
                + " " + d.y + " " + d.x;

            return path;
        }
    }

    function update(source) {
        // Compute the new height, function counts total children of root node and sets tree height accordingly.
        // This prevents the layout looking squashed when new nodes are made visible or looking sparse when nodes are removed
        // This makes the layout more consistent.
        let levelWidth = [1];
        let childCount = function (level, n) {

            if (n.children && n.children.length > 0) {
                if (levelWidth.length <= level + 1) levelWidth.push(0);

                levelWidth[level + 1] += n.children.length;
                n.children.forEach(function (d) {
                    childCount(level + 1, d);
                });
            }
        };


        childCount(0, root);
        let newHeight = d3.max(levelWidth) * 55; // 25 pixels per line


        // Baum-Layout erzeugen und die Größen zuweisen
        treemap = d3.tree().size([newHeight, viewerWidth]);


        // Berechnung x- und y-Positionen pro Knoten
        let treeData = treemap(root);


        // Compute the new tree layout.
        let nodes = treeData.descendants(),
            links = treeData.descendants().slice(1);


        // Set widths between levels based on maxLabelLength.
        nodes.forEach(function (d) {
            d.y = (d.depth * (maxLabelLength * 10)); //maxLabelLength * 10px
            // alternatively to keep a fixed scale one can set a fixed depth per level
            // Normalize for fixed-depth by commenting out below line
            // d.y = (d.depth * 500); //500px per level.
        });

        // 1- Create a tooltip div that is hidden by default:
        var tooltip = d3.select("#tree-container")
            .append("div")
            .style("opacity", 0)
            .attr("class", "tooltip")
            .style("display", "inline-block")
            .style("background-color", "white")
            .style("border", "solid")
            .style("border-width", "3px")
            .style("border-radius", "5px")
            .style("padding", "5px")

        // Three function that change the tooltip when user hover / move / leave a cell
        var mouseover = function (d) {
            tooltip
                .style("opacity", 1)
            d3.select(this)
                .style("stroke", "black")
                .style("opacity", 1)
        }
        var mousemove = function (d) {
            var messageTooltip =
            `
            ${(d.data.artifact != null)
            ?
                `
                Artifact: ${d.data.artifact.artifactId}</br>
                Group: ${d.data.artifact.group}</br>
                Version: ${d.data.artifact.version}</br>
                `
            :
                `Project Module:</br>${d.data.name}`}

            `
            tooltip
                .html(messageTooltip)
                .style("border-color", d.data.style.borderColor)
                .style("left", (d3.event.pageX) + "px")
                .style("top", (d3.event.pageY) + "px");
        }
        var mouseleave = function (d) {
            tooltip
                .style("opacity", 0)
            d3.select(this)
                .style("stroke", d.data.style.borderColor)
                .style("opacity", 1)
        }


        // Update the nodes…
        node = svgGroup.selectAll("g.node").data(nodes, function (d) { return d.id || (d.id = ++i); });


        // Enter any new nodes at the parent's previous position.
        let nodeEnter = node.enter().append("g")//.call(dragListener)
            .attr("class", "node")
            .attr("transform", function (d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
            .on('click', click);

        nodeEnter.append("circle")
            .attr('class', 'nodeCircle')
            .attr("r", 15)
            .style("fill", function (d) {
                return d._children ? d.data.style.fillColor : d.data.style.collapsedColor;
            })
            .style("stroke", function (d) {
                return d.data.style.borderColor;
            })
            .on("mouseover", mouseover)
            .on("mousemove", mousemove)
            .on("mouseleave", mouseleave);

        nodeEnter.append("text")
            .attr("x", function (d) { return d.children || d._children ? -15 : 15; })
            .attr("dy", "-0.20em")
            .attr('class', 'nodeText')
            .attr("text-anchor", function (d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function (d) {
                return d.data.name;
            })
            .style("fill-opacity", 0);

            /* Create the text for each block */
            nodeEnter.append("text")
              .text(function(d){ return d.data.alreadyRendered ? "♻️" : "" })
              .attr("dy", function(d) { return 15/((15*15)/100); })
              .attr("dx", function(d) { return -10; })
              .style("font-size", function(d) { return 20; })
              .attr("text-anchor", "start");


        // Update the text to reflect whether node has children or not.
        node.select('text')
            .attr("x", function (d) {
                return d.children || d._children ? -15 : 15;
            })
            .attr("text-anchor", function (d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function (d) {
                return d.data.name;
            });


        // Change the circle fill depending on whether it has children and is collapsed
        node.select("circle.nodeCircle")
            .attr("r", 15)
            .style("fill", function (d) {
                return d._children ? d.data.style.fillColor : d.data.style.collapsedColor;
            })
            .style("stroke", function (d) {
                return d.data.style.borderColor;
            });


        // Transition nodes to their new position.
        let nodeUpdate = nodeEnter.merge(node);
        nodeUpdate.transition().duration(duration).attr("transform", function (d) { return "translate(" + d.y + "," + d.x + ")"; });

        // Update the node attributes and style
        nodeUpdate.select('circle.node')
            .attr('r', 6)
            .attr("fill-opacity", "0.7")
            .attr("stroke-opacity", "1")
            .style("fill", function (d) {
                if (d.class === "found") {
                    return d.data.style.linkTraceColor; //red
                } else {
                    return d.data.style.linkColor;
                }
            })
            .style("stroke", function (d) {
                if (d.class === "found") {
                    return d.data.style.linkTraceColor; //red
                } else {
                    return d.data.style.linkColor;
                }
            });

        // Fade the text in
        nodeUpdate.select("text").style("fill-opacity", 1);


        // Transition exiting nodes to the parent's new position.
        let nodeExit = node.exit().transition().duration(duration).attr("transform", function (d) { return "translate(" + source.y + "," + source.x + ")"; }).remove();
        nodeExit.select("circle").attr("r", 0);
        nodeExit.select("text").style("fill-opacity", 0);


        // Update the links…
        let link = svgGroup.selectAll("path.link").data(links, function (d) { return d.id; });


        // Enter any new links at the parent's previous position.
        let linkEnter = link.enter().insert("path", "g").attr("class", "link")
            .attr("d", function (d) {
                let o = { x: source.x0, y: source.y0 };
                return diagonal(o, o);
            })


        // Transition links to their new position.
        let linkUpdate = linkEnter.merge(link);
        // Transition back to the parent element position
        linkUpdate.transition()
            .duration(duration)
            .attr('d', function (d) {
                return diagonal(d, d.parent)
            })
            .style("stroke", function (d) {
                if (d.class === "found") {
                    return d.data.style.linkTraceColor;
                } else {
                    return d.data.style.linkColor;
                }
            });


        // Transition exiting nodes to the parent's new position.
        let linkExit = link.exit().transition().duration(duration).attr("d", function (d) {
            let o = { x: source.x, y: source.y };
            return diagonal(o, o);
        }).remove();


        // Stash the old positions for transition.
        nodes.forEach(function (d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Append a group which holds all nodes and which the zoom Listener can act upon.
    let svgGroup = baseSvg.append("g");

    // Define the root
    //root = treeData;
    //root = d3.hierarchy(treeData, function(d) { return d.children; });
    root.x0 = viewerHeight / 2;
    root.x0 = 200;
    root.y0 = 50;
    // root.y0 = viewerWidth / 2;

    // Collapse all children of roots children before rendering.
    root.children.forEach(function (child) {
        collapse(child);
    });

    // Layout the tree initially and center on the root node.
    update(root);
    centerNode(root);
    select2Data = [];
    select2DataCollectName(root);
    select2DataObject = [];
    select2Data.sort(function (a, b) {
        if (a.name > b.name) return 1; // sort
        if (a.name < b.name) return -1;
        return 0;
    })
//        .filter((data) => !data.alreadyRendered)
        .filter(function (item, i, ar) {
            return i === ar.findIndex((t) => (
                t.name === item.name
            ))
        }) // remove duplicate items
        .filter(function (item, i, ar) {
            select2DataObject.push({
                "id": i,
                "text": item.name
            });
        });
    $("#search").scroll(function () {
        $('#FixedDiv').css('top', $(this).scrollTop());
    }).select2({
        placeholder: "Select a Leaf...",
        data: select2DataObject,
        containerCssClass: "search",
        theme: "classic"
    });
};
