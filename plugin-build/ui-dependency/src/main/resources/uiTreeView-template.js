const treeJson = `%json_here%`;
const json = JSON.parse(treeJson);
process(json);
$("select").val('').change();

function process(treeData) {
    // General purpose variables
    let totalNodes = 0, maxLabelLength = 0, i = 0;
    let selectedNode = null, draggingNode = null;
    const panSpeed = 200, panBoundary = 20;
    const duration = 750;
    let root = d3.hierarchy(treeData, d => d.children);
    let cacheRoot = root;
    const viewerWidth = window.innerWidth;
    const viewerHeight = window.innerHeight;

    let dragStarted = null;
    let panTimer = null;
    let nodes = null, tree = null, source = null, node = null;
    let t = null, x = null, y = null;
    let treemap = null;

    // Helper: Recursively visit all nodes in tree
    function visit(parent, visitFn, childrenFn) {
        if (!parent) return;
        visitFn(parent);
        const children = childrenFn(parent);
        if (children) children.forEach(child => visit(child, visitFn, childrenFn));
    }

    // Calculate totalNodes and maxLabelLength
    visit(treeData, d => {
        totalNodes++;
        maxLabelLength = Math.max(d.name.length, maxLabelLength);
    }, d => d.children && d.children.length > 0 ? d.children : null);

    // Tree search utilities
    function searchTree(d, searchField, searchText) {
        if (d.children) d.children.forEach(child => searchTree(child, searchField, searchText));
        if (d._children) d._children.forEach(child => searchTree(child, searchField, searchText));
        let value = searchField.split('.').reduce((acc, key) => acc && acc[key], d);
        if (value && value.toLowerCase().includes(searchText.toLowerCase())) {
            let parent = d;
            while (parent) {
                parent.class = "found";
                parent = parent.parent;
            }
        }
    }

    function collapseAllNotFound(d) {
        if (d.children) {
            if (d.class !== "found") {
                d._children = d.children;
                d._children.forEach(collapseAllNotFound);
                d.children = null;
            } else {
                d.children.forEach(collapseAllNotFound);
            }
        }
    }

    function clearAll(d) {
        d.class = "";
        if (d.children) d.children.forEach(clearAll);
        if (d._children) d._children.forEach(clearAll);
    }

    function expandAll(d) {
        if (d._children) {
            d.children = d._children;
            d.children.forEach(expandAll);
            d._children = null;
        } else if (d.children) {
            d.children.forEach(expandAll);
        }
    }

    // UI event handlers
    $("#btnReset").on("click", function() {
        d3.select("g").selectAll("*").remove();
        root.children.forEach(function reset(d){
            if (d.children) {
                d.class = null;
                d._children = d.children;
                d._children.forEach(reset);
                d.children = null;
            }
        });
        update(root);
        centerNode(root);
        $("select").val('').change();
    });

    $("#search").on("select2:select", function (e) {
        clearAll(root);
        expandAll(root);
        update(root);
        const searchField = "data.name";
        const searchText = e.params.data.text;
        searchTree(root, searchField, searchText);
        root.children.forEach(collapseAllNotFound);
        update(root);
        centerNode(root);
    });

    // Find parent utility (used for breadcrumbs, etc.)
    function findParent(datum) {
        return datum.depth < 2 ? datum.data.name : findParent(datum.parent);
    }

    // Tree visualization code
    function zoom() {
        if (d3.event.transform) {
            svgGroup.attr("transform", d3.event.transform);
        }
    }

    function centerNode(source) {
        t = d3.zoomTransform(baseSvg.node());
        x = -source.y0 * t.k + viewerWidth / 4;
        y = -source.x0 * t.k + viewerHeight / 4;
        d3.select('svg').transition().duration(duration)
            .call(zoomListener.transform, d3.zoomIdentity.translate(x, y).scale(t.k));
    }

    const zoomListener = d3.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);

    let baseSvg = d3.select("#tree-container").append("svg")
        .style("background-color", "white")
        .attr("class", "overlay")
        .call(zoomListener);

    let svgGroup = baseSvg.append("g");

    function collapse(d) {
        if (d.children) {
            d._children = d.children;
            d._children.forEach(collapse);
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

    const overCircle = d => { selectedNode = d; updateTempConnector(); };
    const outCircle = d => { selectedNode = null; updateTempConnector(); };

    function updateTempConnector() {
        let data = [];
        if (draggingNode && selectedNode) {
            data = [{
                source: { x: selectedNode.y0, y: selectedNode.x0 },
                target: { x: draggingNode.y0, y: draggingNode.x0 }
            }];
        }
        let link = svgGroup.selectAll(".templink").data(data);
        link.enter().append("path")
            .attr("class", "templink")
            .attr("d", d => diagonal(d.source, d.source))
            .attr('pointer-events', 'none');
        link.attr("d", d => diagonal(d.source, d.source));
        link.exit().remove();
    }

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

    function click(d) {
        d = toggleChildren(d);
        update(d);
        centerNode(d);
    }

    function diagonal(s, d) {
        if (s && d) {
            return `M ${s.y} ${s.x} C ${(s.y + d.y) / 2} ${s.x}, ${(s.y + d.y) / 2} ${d.x}, ${d.y} ${d.x}`;
        }
    }

    function update(source) {
        let levelWidth = [1];
        function childCount(level, n) {
            if (n.children && n.children.length > 0) {
                if (levelWidth.length <= level + 1) levelWidth.push(0);
                levelWidth[level + 1] += n.children.length;
                n.children.forEach(d => childCount(level + 1, d));
            }
        }
        childCount(0, root);
        const newHeight = d3.max(levelWidth) * 55;

        treemap = d3.tree().size([newHeight, viewerWidth]);
        const treeData = treemap(root);
        let nodes = treeData.descendants();
        let links = treeData.descendants().slice(1);

        nodes.forEach(d => {
            d.y = d.depth * (maxLabelLength * 10);
        });

        // Tooltip
        let tooltip = d3.select("#tree-container")
            .selectAll("div.tooltip").data([0])
            .join("div")
            .attr("class", "tooltip")
            .style("opacity", 0)
            .style("display", "inline-block")
            .style("background-color", "white")
            .style("border", "solid 3px #ccc")
            .style("border-radius", "5px")
            .style("padding", "5px");

        // Tooltip Handlers
        function mouseover(d) {
            tooltip.style("opacity", 1);
            d3.select(this).style("stroke", "black").style("opacity", 1);
        }
        function mousemove(d) {
            let html = d.data.artifact ?
                `Artifact: ${d.data.artifact.artifactId}<br>
                Group: ${d.data.artifact.group}<br>
                Version: ${d.data.artifact.version}<br>`
                : `Project Module:<br>${d.data.name}`;
            tooltip
                .html(html)
                .style("border-color", d.data.style.borderColor)
                .style("left", `${d3.event.pageX}px`)
                .style("top", `${d3.event.pageY}px`);
        }
        function mouseleave(d) {
            tooltip.style("opacity", 0);
            d3.select(this)
                .style("stroke", d.data.style.borderColor)
                .style("opacity", 1);
        }

        // Update nodes
        let node = svgGroup.selectAll("g.node")
            .data(nodes, d => d.id || (d.id = ++i));
        let nodeEnter = node.enter().append("g")
            .attr("class", "node")
            .attr("transform", d => `translate(${source.y0},${source.x0})`)
            .on('click', click);

        nodeEnter.append("circle")
            .attr('class', 'nodeCircle')
            .attr("r", 15)
            .style("fill", d => d._children ? d.data.style.fillColor : d.data.style.collapsedColor)
            .style("stroke", d => d.data.style.borderColor)
            .on("mouseover", mouseover)
            .on("mousemove", mousemove)
            .on("mouseleave", mouseleave);

        nodeEnter.append("text")
            .attr("x", d => d.children || d._children ? -15 : 15)
            .attr("dy", "-0.20em")
            .attr('class', 'nodeText')
            .attr("text-anchor", d => d.children || d._children ? "end" : "start")
            .text(d => d.data.name)
            .style("fill-opacity", 0);

        nodeEnter.append("text")
            .text(d => d.data.alreadyRendered ? "♻️" : "")
            .attr("dy", 15 / ((15 * 15) / 100))
            .attr("dx", -10)
            .style("font-size", 20)
            .attr("text-anchor", "start");

        // Update text and circle
        node.select('text')
            .attr("x", d => d.children || d._children ? -15 : 15)
            .attr("text-anchor", d => d.children || d._children ? "end" : "start")
            .text(d => d.data.name);
        node.select("circle.nodeCircle")
            .attr("r", 15)
            .style("fill", d => d._children ? d.data.style.fillColor : d.data.style.collapsedColor)
            .style("stroke", d => d.data.style.borderColor);

        // Transition nodes to their new position
        nodeEnter.merge(node)
            .transition().duration(duration)
            .attr("transform", d => `translate(${d.y},${d.x})`);
        nodeEnter.merge(node).select("text").style("fill-opacity", 1);

        // Transition exiting nodes to parent's new position
        node.exit().transition().duration(duration)
            .attr("transform", d => `translate(${source.y},${source.x})`).remove()
            .select("circle").attr("r", 0);
        node.exit().select("text").style("fill-opacity", 0);

        // Update links
        let link = svgGroup.selectAll("path.link")
            .data(links, d => d.id);
        let linkEnter = link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", d => diagonal({ x: source.x0, y: source.y0 }, { x: source.x0, y: source.y0 }));

        linkEnter.merge(link).transition().duration(duration)
            .attr("d", d => diagonal(d, d.parent))
            .style("stroke", d => d.class === "found" ? d.data.style.linkTraceColor : d.data.style.linkColor);

        link.exit().transition().duration(duration)
            .attr("d", d => diagonal({ x: source.x, y: source.y }, { x: source.x, y: source.y })).remove();

        // Stash old positions for transition.
        nodes.forEach(d => {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    // Initialize root, collapse children, and render tree
    root.x0 = 200;
    root.y0 = 50;
    root.children.forEach(collapse);
    update(root);
    centerNode(root);

    // Set up select2 search data
    let select2Data = [];
    function select2DataCollectName(d) {
        if (d.children) d.children.forEach(select2DataCollectName);
        if (d._children) d._children.forEach(select2DataCollectName);
        if (!d.children) select2Data.push(d.data);
    }
    select2DataCollectName(root);

    // Remove duplicate names
    let uniqueData = Array.from(new Map(select2Data.map(item => [item.name, item])).values());
    let select2DataObject = uniqueData.map((item, i) => ({
        id: i,
        text: item.name
    }));

    $("#search").scroll(function () {
        $('#FixedDiv').css('top', $(this).scrollTop());
    }).select2({
        placeholder: "Select a Leaf...",
        data: select2DataObject,
        containerCssClass: "search",
        theme: "classic"
    });
}
